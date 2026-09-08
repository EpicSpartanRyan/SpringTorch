import asyncio
import os

import torch
from temporalio import activity, worker
from temporalio.client import Client
from transformers import AutoModelForCausalLM, AutoTokenizer

MODEL_NAME = os.getenv("QWEN_MODEL", "Qwen/Qwen2.5-0.5B-Instruct")
TASK_QUEUE = "llm-task-queue"

print(f"Loading Qwen model: {MODEL_NAME}", flush=True)
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForCausalLM.from_pretrained(
	MODEL_NAME,
	torch_dtype="auto",
	device_map="auto",
)
model.eval()
print("Qwen model loaded", flush=True)


def _generate_response(prompt: str) -> str:
	messages = [
		{"role": "system", "content": "Eres un asistente conciso y directo."},
		{"role": "user", "content": prompt},
	]
	text = tokenizer.apply_chat_template(
		messages,
		tokenize=False,
		add_generation_prompt=True,
	)
	model_inputs = tokenizer([text], return_tensors="pt").to(model.device)

	with torch.inference_mode():
		generated_ids = model.generate(
			**model_inputs,
			max_new_tokens=256,
			do_sample=True,
			temperature=0.7,
		)

	new_tokens = [
		output_ids[len(input_ids) :]
		for input_ids, output_ids in zip(model_inputs.input_ids, generated_ids)
	]
	return tokenizer.batch_decode(new_tokens, skip_special_tokens=True)[0]


@activity.defn(name="generate_qwen_response")
async def generate_qwen_response(prompt: str) -> str:
	return await asyncio.to_thread(_generate_response, prompt)


async def main() -> None:
	address = os.getenv("TEMPORAL_ADDRESS", "localhost:7233")
	client = None
	for attempt in range(1, 11):
		try:
			print(f"Connecting to Temporal at {address} (attempt {attempt}/10)", flush=True)
			client = await asyncio.wait_for(Client.connect(address), timeout=15)
			break
		except Exception as error:
			print(f"Temporal connection failed: {error}", flush=True)
			if attempt == 10:
				raise
			await asyncio.sleep(3)

	assert client is not None
	print("Connected to Temporal; creating worker", flush=True)
	temporal_worker = worker.Worker(
		client,
		task_queue=TASK_QUEUE,
		workflows=[],
		activities=[generate_qwen_response],
		max_concurrent_activities=1,
	)
	print(f"Qwen worker listening on {TASK_QUEUE} via {address}", flush=True)
	await temporal_worker.run()


if __name__ == "__main__":
	asyncio.run(main())
