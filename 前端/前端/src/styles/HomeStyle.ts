import { makeStyles, tokens } from "@fluentui/react-components";


export const useStyles = makeStyles({
  dialogContentSpacing: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  container: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalXL,
  },
  welcomeSection: {
    padding: tokens.spacingVerticalL,
    
    borderRadius: tokens.borderRadiusLarge,
  },
  mainContent: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  noticeSection: {
    display: "flex",
    gap: tokens.spacingVerticalM,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  noticeIcon: {
    color: tokens.colorBrandBackground,
  },
  noticeCard: {
    backgroundImage: "url('/media/notice_bg.svg')",
    backgroundSize: "cover",
    backgroundPosition: "center",
    minHeight: "180px",
    position: "relative",
    overflow: "hidden",
  },
  slider: {
    gap: "10px",
    scrollSnapType:"x mandatory"
  },
  wireframe: {
    borderRadius: tokens.borderRadiusLarge,
    flex: "0 0 100%",   // 非常关键
    placeContent: "center",
    height: "400px",
    position: "relative",
    scrollSnapAlign: "center"
  },
  noticeImage: {
     width: "100%",
    height: "100%",
    objectFit: "cover",
    borderRadius: tokens.borderRadiusLarge,
  },
  contentContainer: {
    display: "grid",
    gridTemplateColumns: "3fr 2fr 2fr",
    gap: tokens.spacingHorizontalXL,
    marginTop: tokens.spacingVerticalL,
    "@media (max-width: 780px)": {
      gridTemplateColumns: "1fr",
    },
  },
  contentColumn: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalL,
  },
  statsSection: {
    display: "flex",
    flexDirection: "column",
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
  },
  sectionHeader: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalS,
  },
  sectionIcon: {
    color: tokens.colorBrandBackground,
  },
  statsGrid: {
    display: "flex",
    flexWrap: "wrap",
    gap: tokens.spacingVerticalM,
  },
  statNote: {
    display: "flex",
    gap: tokens.spacingHorizontalM,
    padding: tokens.spacingVerticalM,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusMedium,
    transition: "all 0.2s ease",
    fontFamily: tokens.fontFamilyBase,
    marginBottom: tokens.spacingVerticalS,
    "&:hover": {
      backgroundColor: tokens.colorNeutralBackground3,
    },
  },
  stattext: {
    fontSize: "24px",
    lineHeight: "1",
    fontFamily: tokens.fontFamilyBase,
  },
  statCard: {
    display: "flex",
    alignItems: "center",
    gap: tokens.spacingHorizontalM,
    padding: tokens.spacingVerticalM,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusMedium,
    flex: "1 1 200px",
    transition: "all 0.2s ease",
    "&:hover": {
      backgroundColor: tokens.colorNeutralBackground3,
    },
  },
  statImage: {
    width: "40px",
    height: "40px",
    objectFit: "contain",
  },
  statContent: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
  },
  statNumber: {
    fontSize: "20px",
    lineHeight: "1",
    fontFamily: tokens.fontFamilyBase,
    fontWeight: "normal",
  },
  statLabel: {
    color: tokens.colorNeutralForeground2,
    fontSize: "12px",
    marginTop: tokens.spacingVerticalXXS,
    fontFamily: tokens.fontFamilyBase,
  },
  addMoreCard: {
    border: `2px dashed ${tokens.colorNeutralStroke2}`,
    backgroundColor: "transparent",
    "&:hover": {
      backgroundColor: tokens.colorNeutralBackground1,
    },
  },
  addMoreIcon: {
    color: tokens.colorBrandBackground,
  },
  dialogContent: {
    display: "flex",
    flexDirection: "column",
  },
  checkToggle: {
    position: "absolute",
    right: "16px",
  
    width: "18px",
    height: "18px",
    borderRadius: "50%",
    border: "2px solid var(--colorNeutralStroke2)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    marginRight: "12px",
    cursor: "pointer",
    transition:
      "background-color 0.2s ease, border-color 0.2s ease, transform 0.15s ease",
    flexShrink: 0,
  } ,
checkToggleActive: {
  backgroundColor: "green",
  transform: "scale(1.05)",
},
checkIcon: {
  color: "#FFF",          // 白色对勾
  opacity: 1,
  transform: "scale(1)",
  transition: "opacity 0.15s ease, transform 0.15s ease",
},
statText: {
  fontSize: "14px",
  fontWeight: 500,
  color: "var(--colorNeutralForeground1)",
},
memoContent:{
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "20px"
},

checkIconActive:{
  opacity: 1,
  transform: "scale(1)",
},

memoText: {
  position: "relative",
  color: "var(--colorNeutralForeground1)",
  transition: "color 0.2s ease, opacity 0.2s ease",
  fontSize: "15px",

  "&::after": {
    content: '""',
    position: "absolute",
    left: 0,
    fontSize: "15px",
    top: "50%",
    width: "100%",
    height: "2px",
    backgroundColor: "var(--colorNeutralForeground3)",
    transform: "scaleX(0)",
    transformOrigin: "left center",
    transition: "transform 0.25s ease",
  },
},
memoTextCompleted: {
  color: "var(--colorNeutralForeground3)",
  opacity: 0.85,
  fontSize: "15px",
  "&::after": {
    transform: "scaleX(1)",
    fontSize: "15px",
  },
},


});
