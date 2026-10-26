import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  root: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    height: "100%",
    width: "100%",
    backgroundColor: tokens.colorNeutralBackground1,
    gap: tokens.spacingVerticalL,
    textAlign: "center",
    padding: tokens.spacingHorizontalL,
    boxSizing: "border-box",
  },
  errorCode: {
    fontSize: "96px",
    fontWeight: tokens.fontWeightBold,
    color: tokens.colorBrandForeground1,
    lineHeight: 1,
    margin: 0,
  },
  title: {
    fontSize: tokens.fontSizeHero700,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorNeutralForeground1,
    margin: 0,
  },
  description: {
    fontSize: tokens.fontSizeBase400,
    color: tokens.colorNeutralForeground2,
    maxWidth: "700px",
    margin: `0 ${tokens.spacingVerticalL} 0 ${tokens.spacingVerticalL}`,
    lineHeight: tokens.lineHeightBase400,
  },
  buttonContainer: {
    display: "flex",
    gap: tokens.spacingHorizontalM,
    marginTop: tokens.spacingVerticalL,
  },
  icon: {
    fontSize: "64px",
    color: tokens.colorPaletteRedForeground1,
  },
});
