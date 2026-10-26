import { makeStyles, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  container: {
    position: "fixed",
    inset: 0,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    backgroundSize: "cover",
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    zIndex: 0,
  },
  containerLight: {
    backgroundImage: "url('/images/login_bg_light.jpg')",
  },
  containerDark: {
    backgroundImage: "url('/images/login_bg_dark.jpg')",
  },
  cardContainer: {
    position: "relative",
    zIndex: 2,
    width: "400px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
  },
  loginCard: {
    width: "100%",
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow64,
    borderRadius: "12px",
    display: "flex",
    flexDirection: "column",
    padding: tokens.spacingVerticalXXXL,
  },
  cardContent: {
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalXXL}`,
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXL,
    boxSizing: "border-box",
  },
  header: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: tokens.spacingVerticalM,
  },
  backButton: {
    position: "absolute",
    top: tokens.spacingVerticalL,
    left: tokens.spacingVerticalL,
    minWidth: "auto",
    padding: tokens.spacingHorizontalS,
  },
  logo: {
    fontSize: "48px",
    color: tokens.colorBrandBackground,
  },
  badge: {
    minWidth: "100px",
    minHeight: "30px",
    marginTop: tokens.spacingVerticalM,
  },
  title1: {
    textAlign: "center",
    color: tokens.colorNeutralForeground1,
    marginBottom: tokens.spacingVerticalM,
  },
  title2: {
    textAlign: "center",
    color: tokens.colorNeutralForeground1,
    marginTop: tokens.spacingVerticalM,
    marginBottom: tokens.spacingVerticalXXL,
  },
  body: {
    textAlign: "center",
    color: tokens.colorNeutralForeground1,
    marginBottom: tokens.spacingVerticalM,
  },
  link: {
    width: "100%",
    textAlign: "left",
    marginBottom: tokens.spacingVerticalM,
    fontWeight: tokens.fontWeightSemibold,
  },
  inputSection: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalM,
  },
  inputGroup: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalM,
  },
  verificationGroup: {
    display: "flex",
    gap: tokens.spacingHorizontalS,
    alignItems: "flex-end",
  },
  verificationInput: {
    flexGrow: 1,
    width: "100px",
  },
  sendCodeButton: {
    minWidth: "100px",
  },
  errorText: {
    color: tokens.colorPaletteRedForeground1,
    fontSize: tokens.fontSizeBase200,
  },
  linkSection: {
    width: "100%",
    textAlign: "left",
  },
  actionSection: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXL,
    marginTop: tokens.spacingVerticalXL,
  },
  continueButton: {
    width: "100%",
    height: "40px",
  },
  spinnerContainer: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: tokens.spacingHorizontalS,
  },
});
