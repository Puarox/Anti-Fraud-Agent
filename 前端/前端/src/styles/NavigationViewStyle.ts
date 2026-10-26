import { createPresenceComponent, makeStyles, motionTokens, tokens } from "@fluentui/react-components";

export const useStyles = makeStyles({
  nav: {
    width: "260px",
    "@media (max-width: 500px)": {
      width: "100%",
    },
  },
  navDashboard: {
    paddingTop: "48px",
    height: "100%",
    boxSizing: "border-box",
  },
  footer: {
    width: "94.5%",
    margin: ` 0 ${tokens.spacingVerticalM} 0 ${tokens.spacingVerticalMNudge}`,
    paddingBottom: tokens.spacingVerticalXL,
  },
});

export const DrawerMotion = createPresenceComponent(() => {
  const keyframes = [
    {
      opacity: 0,
      marginLeft: "-260px",
    },
    {
      opacity: 1,
      marginLeft: "0",
    },
  ];

  return {
    enter: {
      keyframes,
      duration: motionTokens.durationSlow,
      easing: motionTokens.curveDecelerateMin,
    },
    exit: {
      keyframes: [...keyframes].reverse(),
      duration: motionTokens.durationSlow,
      easing: motionTokens.curveAccelerateMin,
    },
  };
});
