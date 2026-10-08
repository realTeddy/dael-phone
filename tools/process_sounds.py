"""Trim each recording to its loudest ~2.5 s, fade, loudness-normalize, write OGG into res/raw."""
import subprocess, sys, os, numpy as np
src_dir, out_dir = sys.argv[1], sys.argv[2]
SR, WIN = 22050, 2.5
for f in sorted(os.listdir(src_dir)):
    key = f.rsplit(".", 1)[0]
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", os.path.join(src_dir, f), "-ac", "1", "-ar", str(SR), "-f", "f32le", "-"], capture_output=True).stdout
    x = np.frombuffer(raw, dtype=np.float32)
    dur = len(x) / SR
    if dur <= WIN + 0.2:
        start = 0.0; length = dur
    else:
        hop = int(0.1 * SR); w = int(WIN * SR)
        energy = [float(np.sqrt(np.mean(x[i:i + w] ** 2))) for i in range(0, len(x) - w, hop)]
        best = int(np.argmax(energy)) * hop
        # back up a little so the onset is not chopped
        start = max(0.0, best / SR - 0.3); length = WIN + 0.3
    out = os.path.join(out_dir, f"s_{key}.ogg")
    af = f"afade=t=in:d=0.05,afade=t=out:st={max(0, length - 0.25)}:d=0.25,loudnorm=I=-16:TP=-1.5:LRA=11"
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-ss", f"{start:.2f}", "-t", f"{length:.2f}", "-i", os.path.join(src_dir, f),
                    "-ac", "1", "-ar", "24000", "-af", af, "-c:a", "libvorbis", "-q:a", "4", out], check=True)
    print(f"{key:10s} src {dur:6.1f}s -> start {start:5.1f}s len {length:.1f}s")
