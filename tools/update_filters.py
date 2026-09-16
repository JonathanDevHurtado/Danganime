#!/usr/bin/env python3
"""
Regenera las listas de bloqueo de Danganime a partir de las fuentes de uBlock Origin.

Genera:
  android/app/src/main/assets/filters/blocked_domains.txt   (~98k dominios)

Uso:
  python3 tools/update_filters.py            # descarga las listas y regenera
  python3 tools/update_filters.py --local     # usa los .txt que haya en tools/lists/

El CSS cosmético (cosmetic_selectors.txt) es curado manualmente y no se sobreescribe.
"""

import os
import re
import sys
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
OUT = os.path.join(ROOT, "android", "app", "src", "main", "assets", "filters", "blocked_domains.txt")
LISTS_DIR = os.path.join(HERE, "lists")

SOURCES = {
    "easylist.txt": "https://easylist.to/easylist/easylist.txt",
    "easyprivacy.txt": "https://easylist.to/easylist/easyprivacy.txt",
    "ublock-filters.txt": "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters.txt",
    "ublock-privacy.txt": "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/privacy.txt",
    "ublock-unbreak.txt": "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/unbreak.txt",
    "ublock-badware.txt": "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/badware.txt",
}

# Bases que NUNCA se bloquean (CDNs, reproductores y megaservicios).
# Debe coincidir con SAFE_BASES de MainActivity.java
SAFE_BASES = [
    "cloudflare.com", "googleapis.com", "unpkg.com", "jsdelivr.net", "gstatic.com",
    "amazonaws.com", "amazon.com", "cloudfront.net", "akamai.net", "akamaized.net",
    "fastly.net", "googleusercontent.com", "youtube.com", "googlevideo.com",
    "github.io", "githubusercontent.com", "microsoft.com", "apple.com", "google.com",
    "facebook.com", "twitter.com", "x.com", "instagram.com", "whatsapp.com",
    "youtube-nocookie.com", "bootstrapcdn.com", "wp.com", "wixstatic.com",
    "squarespace.com", "vercel.app", "netlify.app", "web.app",
    "jwpcdn.com", "jwplayer.com", "jwplatform.com",
    "streamtape.com", "streamtape.net", "streamtape.to", "streamtape.xyz",
    "streamtape.cc", "strtape.cloud", "strcloud.in",
    "ok.ru", "vk.com", "vk.ru", "rutube.ru", "dailymotion.com", "vimeo.com",
    "yourupload.com", "netu.ac",
    "earnvids.com", "savefiles.com", "vidara.to", "saidochesto.top",
    "streamwish.to", "streamwish.com", "streamwish.xyz", "streamwish.online",
    "streamwish.top", "streamwish.net", "streamwish.pw",
    "hexupload.net", "hexupload.com",
    "mixdrop.co", "mixdrop.to", "mixdrop.ag", "mixdrop.sx", "mixdrop.bz", "mixdrop.ch",
    "filemoon.sx", "filemoon.to", "filemoon.in",
    "mp4upload.com", "vidsonic.net", "vidsonic.com",
    "zoplayer.com", "zoplayer.to", "zplayer.live",
    "luluvdo.com", "luluvdo.net", "imgur.com", "ibb.co", "gyazo.com",
    "streamnova.to", "streamnova.net", "streamnova-zone.com",
]

HOST_RE = re.compile(r"^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
FILTER_RE = re.compile(r"^\|\|([a-z0-9][a-z0-9.\-]*)\^?\s*$")


def is_safe(domain: str) -> bool:
    return any(domain == b or domain.endswith("." + b) for b in SAFE_BASES)


def download(name: str, url: str) -> str:
    os.makedirs(LISTS_DIR, exist_ok=True)
    path = os.path.join(LISTS_DIR, name)
    print(f"  descargando {name} ...")
    req = urllib.request.Request(url, headers={"User-Agent": "Danganime-filters/1.0"})
    with urllib.request.urlopen(req, timeout=60) as r, open(path, "wb") as f:
        f.write(r.read())
    return path


def local_path(name: str) -> str:
    return os.path.join(LISTS_DIR, name)


def parse_file(path: str, domains: set) -> None:
    with open(path, encoding="utf-8", errors="ignore") as f:
        for line in f:
            line = line.strip()
            if not line or line[0] in "!#" or line.startswith("@@"):
                continue
            m = FILTER_RE.match(line)
            if not m:
                continue
            d = m.group(1).lower()
            if len(d) <= 63 and HOST_RE.match(d) and not is_safe(d):
                domains.add(d)


def main() -> None:
    use_local = "--local" in sys.argv
    domains: set = set()

    for name, url in SOURCES.items():
        path = local_path(name) if use_local else download(name, url)
        if os.path.exists(path):
            parse_file(path, domains)
        else:
            print(f"  aviso: no existe {path}")

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(sorted(domains)) + "\n")

    print(f"\nOK -> {OUT}")
    print(f"   {len(domains)} dominios escritos")


if __name__ == "__main__":
    main()
