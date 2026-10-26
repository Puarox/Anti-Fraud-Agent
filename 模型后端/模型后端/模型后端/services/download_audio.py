from .safe_download import download_media

def download_audio(url: str) -> str:
    return download_media(url, ".mp3")
