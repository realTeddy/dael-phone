"""Find the phone's wireless-debugging port when mDNS is unavailable: python3 -I tools/dev/scan.py <ip>[,<ip>]"""
import asyncio, sys
hosts = sys.argv[1].split(',')
sem = asyncio.Semaphore(1500)
async def probe(h, p):
    async with sem:
        try:
            r, w = await asyncio.wait_for(asyncio.open_connection(h, p), 0.5)
            w.close(); print(f"open {h}:{p}", flush=True)
        except Exception: pass
async def main():
    await asyncio.gather(*(probe(h, p) for h in hosts for p in range(30000, 50000)))
asyncio.run(main())
