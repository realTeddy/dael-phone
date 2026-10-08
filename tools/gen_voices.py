#!/usr/bin/env python3
"""Generates the app's spoken phrases as natural-sounding clips with Kokoro TTS.

Every phrase the app can say is listed here. The app looks up a clip by a slug of the
exact phrase text (see Speaker.kt); anything without a clip falls back to system TTS.

Setup (once):   cd tools && uv init -q --python 3.12 tts && cd tts
                uv add torch --index pytorch-cpu=https://download.pytorch.org/whl/cpu
                uv add kokoro soundfile "transformers>=4.45"
Run:            uv run python ../gen_voices.py --child Dael
Needs ffmpeg on PATH for the OGG conversion.
"""
import argparse, os, re, subprocess, sys, tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(HERE, "..", "app", "src", "main", "res", "raw")

FEMALE, MALE, NARRATOR = "af_heart", "am_michael", "af_heart"
FEMALE_NAMES = ["Mom", "Mama", "Mommy", "Grandma", "Nana", "Auntie", "Sister"]
MALE_NAMES = ["Dad", "Papa", "Daddy", "Grandpa", "Uncle", "Brother"]
ANIMALS = [("Dog", "Woof woof!"), ("Cat", "Meow!"), ("Cow", "Moo!"), ("Pig", "Oink oink!"),
           ("Chicken", "Cluck cluck!"), ("Duck", "Quack quack!"), ("Sheep", "Baa!"), ("Horse", "Neigh!"),
           ("Lion", "Roar!"), ("Elephant", "Toot!"), ("Frog", "Ribbit!"), ("Monkey", "Ooh ooh ah ah!")]
CHARACTERS = [("Lion", "Roar! Roar!", MALE), ("Frog", "Ribbit ribbit!", FEMALE), ("Cow", "Moo! Moo!", FEMALE),
              ("Chicken", "Cluck cluck cluck!", FEMALE), ("Puppy", "Woof woof!", FEMALE), ("Kitty", "Meow meow!", FEMALE),
              ("Fire truck", "Nee naw nee naw!", MALE), ("Train", "Choo choo!", MALE), ("Robot", "Beep boop beep!", MALE)]
DIGITS = ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "star", "heart"]


def slug(text: str) -> str:
    """Must match Speaker.slug() in the app."""
    s = re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")
    return "v_" + s[:60].rstrip("_")


def phrases(child: str):
    out = []
    for d in DIGITS:
        out.append((d, NARRATOR))
    for name, sound in ANIMALS:
        out.append((f"{name}. {sound}", NARRATOR))
    for name, line, voice in CHARACTERS:
        out.append((f"Hello {child}! This is the {name}!", voice))
    out.append(("Bye bye!", FEMALE))
    for name in FEMALE_NAMES + MALE_NAMES:
        voice = MALE if name in MALE_NAMES else FEMALE
        out.append((f"Calling {name}", NARRATOR))
        out.append((f"{name} is calling!", NARRATOR))
        out.append((f"Hi {child}! It's {name}! I love you! Bye bye!", voice))
        out.append((f"Peekaboo! It's {name}!", NARRATOR))
        out.append((f"Where did {name} go?", NARRATOR))
    out.append(("Peekaboo! It's you!", NARRATOR))
    out.append(("Where did everyone go?", NARRATOR))
    return out


def synth(pipe, text, voice, np):
    """Single words come out clipped when synthesized alone ("six" loses its final consonant), so short
    phrases are generated with a continuation sentence and cut at the phrase's end timestamp."""
    if len(text.split()) <= 2:
        results = list(pipe(f"{text}. Okay then.", voice=voice, speed=0.95))
        tokens = [t for r in results for t in (r.tokens or [])]
        full = np.concatenate([r.audio.numpy() for r in results])
        n_words = len(text.split())
        end = tokens[n_words - 1].end_ts if len(tokens) >= n_words else None
        if end:
            audio = full[: int((end + 0.15) * 24000)]
        else:
            audio = np.concatenate([r.audio.numpy() for r in pipe(text, voice=voice, speed=0.95)])
    else:
        audio = np.concatenate([r.audio.numpy() for r in pipe(text, voice=voice, speed=0.95)])
    pad = np.zeros(int(0.3 * 24000), dtype=audio.dtype)
    return np.concatenate([pad[: int(0.1 * 24000)], audio, pad])


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--child", default="Dael")
    ap.add_argument("--only-missing", action="store_true")
    ap.add_argument("--filter", default="", help="only phrases containing this text")
    args = ap.parse_args()

    from kokoro import KPipeline
    import numpy as np
    import soundfile as sf

    os.makedirs(RAW, exist_ok=True)
    pipe = KPipeline(lang_code="a", repo_id="hexgrad/Kokoro-82M")
    todo = phrases(args.child)
    print(f"{len(todo)} phrases")
    with tempfile.TemporaryDirectory() as tmp:
        for i, (text, voice) in enumerate(todo, 1):
            name = slug(text)
            target = os.path.join(RAW, name + ".ogg")
            if args.only_missing and os.path.exists(target):
                continue
            if args.filter and args.filter not in text:
                continue
            audio = synth(pipe, text, voice, np)
            wav = os.path.join(tmp, name + ".wav")
            sf.write(wav, audio, 24000)
            subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-i", wav, "-c:a", "libvorbis", "-q:a", "3", target], check=True)
            print(f"[{i}/{len(todo)}] {name}  <- {text!r}")


if __name__ == "__main__":
    main()
