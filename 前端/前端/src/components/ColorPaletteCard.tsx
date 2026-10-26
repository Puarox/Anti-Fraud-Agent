import type { BrandVariants } from "@fluentui/react-components";
import {
  Body1,
  Button,
  Caption1,
  Card,
  CardHeader,
  Divider,
  Field,
  Input,
  Radio,
  RadioGroup,
  Slider,
  Text,
  makeStyles,
  tokens,
} from "@fluentui/react-components";
import { useEffect, useState } from "react";
import { contrast } from "../utils/theme/csswg";
import { getBrandTokensFromPalette } from "../utils/theme/getBrandTokensFromPalette";
import { hex_to_sRGB } from "../utils/theme/palettes";
import { useTheme } from "./ThemeContext";

const hexCopyClassName = "hexCopy";

const useStyles = makeStyles({
  paletteContainer: {
    display: "flex",
    height: "150px",
    justifyContent: "space-evenly",
    marginBottom: "16px",
  },
  paletteBlock: {
    display: "grid",
    gridTemplateColumns: "0.5em auto",
    gridTemplateRows: "0.5em 1fr 1fr 0.5em",
    flexGrow: 1,
    flexShrink: 0,
    cursor: "pointer",
    transitionProperty: "all",
    transitionDuration: "0.2s",
    [`&:hover .${hexCopyClassName}`]: {
      display: "flex",
    },
    ":hover": {
      flexShrink: 1,
    },
  },
  hexCopy: {
    display: "none",
    justifyContent: "space-between",
    gridColumnStart: 2,
    gridRowStart: 2,
    fontSize: "12px",
  },
  brandKey: {
    justifyContent: "left",
    display: "flex",
    alignItems: "flex-end",
    gridColumnStart: 2,
    gridRowStart: 3,
  },
  colorPicker: {
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    borderRadius: "25px",
    height: "30px",
    width: "30px",
    overflow: "hidden",
    cursor: "pointer",
  },
  colorInput: {
    padding: "0px",
    border: "none",
    opacity: 0,
    width: "100%",
    height: "100%",
    cursor: "pointer",
  },
  controlsGrid: {
    display: "flex",
    flexDirection: "column",
    gap: "16px",
  },
  fieldRow: {
    display: "grid",
    gridTemplateColumns: "135px 30px",
    columnGap: "12px",
    alignItems: "center",
  },
  sliderRow: {
    display: "grid",
    gridTemplateColumns: "115px 50px",
    columnGap: "15px",
    alignItems: "center",
  },
  buttonRow: {
    display: "flex",
    gap: "8px",
    justifyContent: "flex-end",
  },
  colorCard: {
    padding: tokens.spacingVerticalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    boxShadow: tokens.shadow4,
  },
});

const getBrands = (colors: BrandVariants): number[] => {
  return Object.keys(colors).map((color) => parseInt(color, 10));
};

export default function ColorPaletteCard() {
  const styles = useStyles();
  const { setCustomTheme } = useTheme();

  const [keyColor, setKeyColor] = useState<string>("#0F6CBD");
  const [hueTorsion, setHueTorsion] = useState<number>(0);
  const [vibrancy, setVibrancy] = useState<number>(0);
  const [isDark, setIsDark] = useState<boolean>(false);
  const [brandColors, setBrandColors] = useState<BrandVariants>(() => getBrandTokensFromPalette(keyColor));

  useEffect(() => {
    const colors = getBrandTokensFromPalette(keyColor.padEnd(7, "0"), {
      hueTorsion: hueTorsion / 100,
      darkCp: vibrancy / 100,
      lightCp: vibrancy / 100,
    });
    setBrandColors(colors);
  }, [keyColor, hueTorsion, vibrancy]);

  const handleKeyColorChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newHexColor = "#" + e.target.value.replace(/[^0-9A-F]/gi, "").toUpperCase();
    setKeyColor(newHexColor);
  };

  const handleKeyColorBlur = () => {
    setKeyColor((prev) => prev.padEnd(7, "0"));
  };

  const handleApplyTheme = () => {
    setCustomTheme(brandColors, isDark);
  };

  const handleResetTheme = () => {
    setKeyColor("#0F6CBD");
    setHueTorsion(0);
    setVibrancy(0);
  };

  return (
    <Card className={styles.colorCard}>
      <CardHeader header={<Body1>主题色</Body1>} />
      <Divider
        style={{
          margin: "0px -10px 0px -20px",
          width: "calc(100% + 200px)",
        }}
      />

      <div style={{ padding: "12px 0" }}>
        <div className={styles.paletteContainer}>
          {getBrands(brandColors).map((brandKey) => {
            const brandColor = (brandColors as Record<string, string>)[brandKey].toUpperCase();
            const textColor = contrast(hex_to_sRGB(brandColor), hex_to_sRGB("#FFFFFF")) <= 4.5 ? "black" : "white";
            return (
              <div
                key={brandKey}
                className={styles.paletteBlock}
                style={{
                  backgroundColor: brandColor,
                  color: textColor,
                }}
                onClick={() => {
                  navigator.clipboard.writeText(brandColor);
                }}>
                <div className={`${styles.hexCopy} ${hexCopyClassName}`}>
                  <Text size={200}>{brandColor}</Text>
                </div>
                <Text className={styles.brandKey} size={200}>
                  {brandKey}
                </Text>
              </div>
            );
          })}
        </div>

        <div className={styles.controlsGrid}>
          <div className={styles.fieldRow}>
            <Field label="主色值">
              <Input
                appearance="underline"
                value={keyColor}
                onChange={handleKeyColorChange}
                maxLength={7}
                onBlur={handleKeyColorBlur}
              />
            </Field>
            <div className={styles.colorPicker} style={{ backgroundColor: keyColor }}>
              <input
                className={styles.colorInput}
                type="color"
                value={keyColor.padEnd(7, "0")}
                onChange={handleKeyColorChange}
              />
            </div>
          </div>

          <Field label="色相扭曲">
            <div className={styles.sliderRow}>
              <Slider min={-50} max={50} value={hueTorsion} onChange={(_, data) => setHueTorsion(data.value)} />
              <Input
                size="small"
                type="number"
                min={-50}
                max={50}
                appearance="outline"
                value={hueTorsion.toString()}
                onChange={(e) => setHueTorsion(parseInt(e.target.value || "0", 10))}
              />
            </div>
          </Field>

          <Field label="饱和度">
            <div className={styles.sliderRow}>
              <Slider min={-50} max={50} value={vibrancy} onChange={(_, data) => setVibrancy(data.value)} />
              <Input
                size="small"
                type="number"
                min={-50}
                max={50}
                appearance="outline"
                value={vibrancy.toString()}
                onChange={(e) => setVibrancy(parseInt(e.target.value || "0", 10))}
              />
            </div>
          </Field>

          <Field label="主题模式">
            <RadioGroup value={isDark ? "dark" : "light"} onChange={(_, data) => setIsDark(data.value === "dark")}>
              <Radio value="light" label="浅色" />
              <Radio value="dark" label="深色" />
            </RadioGroup>
          </Field>

          <div className={styles.buttonRow}>
            <Button appearance="secondary" onClick={handleResetTheme}>
              重置
            </Button>
            <Button appearance="primary" onClick={handleApplyTheme}>
              应用主题
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );
}
