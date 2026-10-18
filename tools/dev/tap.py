"""Tap a UI element by its text or content description: python3 -I tools/dev/tap.py <serial> <text>"""
import subprocess, sys, re, os, xml.etree.ElementTree as ET
serial, text = sys.argv[1], sys.argv[2]
adb = [os.path.expanduser("~/Android/Sdk/platform-tools/adb"), "-s", serial]
subprocess.run(adb + ["shell", "uiautomator", "dump", "/sdcard/ui.xml"], capture_output=True)
xml = subprocess.run(adb + ["shell", "cat", "/sdcard/ui.xml"], capture_output=True, text=True).stdout
for n in ET.fromstring(xml).iter("node"):
    if n.get("text") == text or n.get("content-desc") == text:
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
        subprocess.run(adb + ["shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2)])
        print(f"tapped {text!r} at {(x1+x2)//2},{(y1+y2)//2}"); break
else:
    print(f"NOT FOUND {text!r}")
