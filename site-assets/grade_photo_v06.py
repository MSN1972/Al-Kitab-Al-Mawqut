#!/usr/bin/env python3
"""Reproducible enhanced full-resolution CC0 Kaaba photograph. No upscaling.
Original photo LaachirDeeper, https://commons.wikimedia.org/wiki/File:Kaaba_Al-Musharrafah.jpg
License: Creative Commons CC0 1.0.
"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageFilter

directory=Path("webos/assets")
source=directory/"kaaba-4k.jpg"
im=Image.open(source).convert("RGB")
if im.size != (4096,2510):
    raise ValueError(f"Unexpected source photo dimensions: {im.size}")
arr=np.asarray(im, dtype=np.float32)
height=arr.shape[0]
ys=np.linspace(0,1,height,dtype=np.float32)[:,None,None]
arr=np.clip((arr-123)*1.16+120,0,255)
luma=arr @ np.array([.2126,.7152,.0722],dtype=np.float32)
arr=luma[...,None]+(arr-luma[...,None])*1.32
alpha=.58*(np.clip((.39-ys)/.39,0,1)**1.35)
arr=arr*(1-alpha)+np.array([10,34,61],dtype=np.float32)[None,None,:]*alpha
horizon=np.clip(1-abs(ys-.255)/.13,0,1)**1.5
arr=arr*(1-.13*horizon)+np.array([247,115,41],dtype=np.float32)[None,None,:]*(.13*horizon)
result=Image.fromarray(np.uint8(np.clip(arr,0,255)),"RGB")
result=result.filter(ImageFilter.UnsharpMask(radius=1.3,percent=105,threshold=3))
out=directory/"kaaba-4k-cinematic.jpg"
result.save(out,quality=90,subsampling=0,optimize=True)
assert Image.open(out).size==(4096,2510)
print("Verified cinematic CC0 photo:",out.stat().st_size, result.size)
