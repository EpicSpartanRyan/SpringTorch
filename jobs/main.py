import asyncio


async def main() -> None:
	await asyncio.Event().wait()


if __name__ == "__main__":
	asyncio.run(main())
