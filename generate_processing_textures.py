#!/usr/bin/env python3
"""
素材加工ステーションのプレースホルダーテクスチャを生成するスクリプト
既存のステーションブロックと同じスタイルで作成

使用方法:
    pip install Pillow
    python generate_processing_textures.py
"""

from PIL import Image, ImageDraw
import os

# 出力ディレクトリ
OUTPUT_DIR = "src/main/resources/assets/anvil/textures/block"

# ベースカラー定義（ステーションブロック風）
COLORS = {
    # 精錬所 - オレンジ/赤系（炉のイメージ）
    "refinery": {
        "primary": (139, 69, 19),      # 茶色ベース
        "secondary": (205, 92, 0),      # オレンジアクセント
        "highlight": (255, 140, 0),     # 明るいオレンジ
        "dark": (101, 67, 33),          # 暗い茶色
        "lit": (255, 100, 0),           # 稼働時の光
    },
    # 鍛造ステーション - グレー/黒系（金床のイメージ）
    "forging_station": {
        "primary": (70, 70, 70),        # ダークグレー
        "secondary": (100, 100, 100),   # ミディアムグレー
        "highlight": (140, 140, 140),   # ライトグレー
        "dark": (40, 40, 40),           # 黒に近いグレー
        "lit": (255, 200, 100),         # 稼働時の光（火花）
    },
    # 研磨ステーション - 白/シルバー系（磨きのイメージ）
    "polishing_station": {
        "primary": (180, 180, 190),     # シルバーグレー
        "secondary": (200, 200, 210),   # 明るいシルバー
        "highlight": (230, 230, 240),   # ハイライト
        "dark": (120, 120, 130),        # 暗いグレー
        "lit": (200, 220, 255),         # 稼働時の光（青白い）
    },
    # パーツ鍛造所 - 緑/青系（クラフトのイメージ）
    "part_forge": {
        "primary": (60, 80, 60),        # 暗い緑
        "secondary": (80, 100, 80),     # ミディアム緑
        "highlight": (100, 140, 100),   # 明るい緑
        "dark": (40, 50, 40),           # 暗い緑
    },
}


def create_station_top(name: str, colors: dict, size: int = 16) -> Image.Image:
    """ステーション上面テクスチャを生成（作業台風）"""
    img = Image.new('RGBA', (size, size), colors["primary"])
    draw = ImageDraw.Draw(img)

    # 外枠（暗い色）
    draw.rectangle([0, 0, size-1, size-1], outline=colors["dark"])

    # 内側のフレーム
    draw.rectangle([1, 1, size-2, size-2], outline=colors["secondary"])

    # 中央のワークエリア（明るい色）
    draw.rectangle([3, 3, size-4, size-4], fill=colors["secondary"])
    draw.rectangle([4, 4, size-5, size-5], fill=colors["highlight"])

    # 角のリベット風装飾
    for x, y in [(2, 2), (size-3, 2), (2, size-3), (size-3, size-3)]:
        draw.point((x, y), fill=colors["dark"])

    return img


def create_station_side(name: str, colors: dict, size: int = 16) -> Image.Image:
    """ステーション側面テクスチャを生成"""
    img = Image.new('RGBA', (size, size), colors["primary"])
    draw = ImageDraw.Draw(img)

    # 上下のボーダー
    draw.line([(0, 0), (size-1, 0)], fill=colors["dark"])
    draw.line([(0, size-1), (size-1, size-1)], fill=colors["dark"])

    # 中央の横線（木目/金属板風）
    for y in [4, 8, 12]:
        draw.line([(0, y), (size-1, y)], fill=colors["secondary"])

    # 縦の支柱
    draw.line([(2, 1), (2, size-2)], fill=colors["dark"])
    draw.line([(size-3, 1), (size-3, size-2)], fill=colors["dark"])

    return img


def create_station_front(name: str, colors: dict, size: int = 16, lit: bool = False) -> Image.Image:
    """ステーション前面テクスチャを生成（炉口/作業口付き）"""
    img = Image.new('RGBA', (size, size), colors["primary"])
    draw = ImageDraw.Draw(img)

    # 上下のボーダー
    draw.line([(0, 0), (size-1, 0)], fill=colors["dark"])
    draw.line([(0, size-1), (size-1, size-1)], fill=colors["dark"])

    # 縦の支柱
    draw.line([(1, 1), (1, size-2)], fill=colors["dark"])
    draw.line([(size-2, 1), (size-2, size-2)], fill=colors["dark"])

    # 中央の開口部/作業口
    opening_color = colors.get("lit", colors["highlight"]) if lit else colors["dark"]
    draw.rectangle([4, 5, size-5, size-4], fill=opening_color)
    draw.rectangle([5, 6, size-6, size-5], fill=colors["highlight"] if lit else (30, 30, 30))

    # 稼働時は光を追加
    if lit and "lit" in colors:
        draw.rectangle([6, 7, size-7, size-6], fill=colors["lit"])

    return img


def create_bottom(size: int = 16) -> Image.Image:
    """共通の底面テクスチャを生成"""
    # 既存のstation_bottom.pngがあるので、それを使用
    # ここでは念のため生成しておく
    img = Image.new('RGBA', (size, size), (80, 60, 40))
    draw = ImageDraw.Draw(img)

    # 木目風のパターン
    for y in range(0, size, 2):
        draw.line([(0, y), (size-1, y)], fill=(70, 50, 30))

    return img


def generate_textures():
    """全てのテクスチャを生成"""
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 各ステーションのテクスチャを生成
    for station_name, colors in COLORS.items():
        print(f"生成中: {station_name}")

        # 上面テクスチャ
        top = create_station_top(station_name, colors)
        top.save(os.path.join(OUTPUT_DIR, f"{station_name}_top.png"))

        # 側面テクスチャ
        side = create_station_side(station_name, colors)
        side.save(os.path.join(OUTPUT_DIR, f"{station_name}_side.png"))

        # 前面テクスチャ（通常）
        front = create_station_front(station_name, colors, lit=False)
        front.save(os.path.join(OUTPUT_DIR, f"{station_name}_front.png"))

        # 前面テクスチャ（稼働時）- lit状態があるステーションのみ
        if "lit" in colors:
            front_on = create_station_front(station_name, colors, lit=True)
            front_on.save(os.path.join(OUTPUT_DIR, f"{station_name}_front_on.png"))

    print("テクスチャ生成完了！")


if __name__ == "__main__":
    generate_textures()
