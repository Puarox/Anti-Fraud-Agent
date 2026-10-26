import {
  Body1,
  Caption1,
  Card,
  CardHeader,
  Divider,
  Image,
  Radio,
  RadioGroup,
  Title1,
} from "@fluentui/react-components";
import ColorPaletteCard from "../components/ColorPaletteCard";
import { useTheme } from "../components/ThemeContext";
import { useStyles } from "../styles/SettingStyle";

export default function SettingsPage() {
  const styles = useStyles();
  const {
    themeMode,
    setLightTheme,
    setDarkTheme,
    setSystemTheme,
    setTeamsLightTheme,
    setTeamsDarkTheme,
    setTeamsHighContrastTheme,
  } = useTheme();

  function handleThemeChange(value: string) {
    switch (value) {
      case "light":
        setLightTheme();
        break;
      case "dark":
        setDarkTheme();
        break;
      case "system":
        setSystemTheme();
        break;
      case "teamsLight":
        setTeamsLightTheme();
        break;
      case "teamsDark":
        setTeamsDarkTheme();
        break;
      case "teamsHighContrast":
        setTeamsHighContrastTheme();
        break;
      case "custom":
      case "customLight":
      case "customDark":
        // Custom theme is applied through ColorPaletteCard
        break;
    }
  }

  return (
    <div className={styles.container}>
      <Title1>设置</Title1>

      <div className={styles.section}>
        <Body1 className={styles.sectionHeader}>
          <b>个性化</b>
        </Body1>

        <Card className={styles.themeCard}>
          <CardHeader header={<Body1>主题</Body1>} />
          <Divider
            style={{
              margin: "0px -10px 0px -20px",
              width: "calc(100% + 200px)",
            }}
          />
          <div>
            <RadioGroup value={themeMode} onChange={(_, data) => handleThemeChange(data.value)}>
              <Radio value="light" label="现代浅色" />
              <Radio value="dark" label="现代深色" />
              <Radio value="teamsLight" label="团队浅色" />
              <Radio value="teamsDark" label="团队深色" />
              <Radio value="teamsHighContrast" label="高对比度" />
              <Radio value="customLight" label="自定义浅色" />
              <Radio value="customDark" label="自定义深色" />
              <Radio value="system" label="使用系统设置" />
            </RadioGroup>
          </div>
        </Card>

        <ColorPaletteCard />
      </div>

      <div className={styles.section}>
        <Body1 className={styles.sectionHeader}>
          <b>关于</b>
        </Body1>

        <Card className={styles.aboutCard}>
          <CardHeader
            image={<Image src="/images/JinJing.png" width={20} height={20} />}
            header={<Body1>金睛</Body1>}
            description={<Caption1>© 2026 如来佛组。保留所有权利。</Caption1>}
          />
        </Card>
      </div>
    </div>
  );
}
