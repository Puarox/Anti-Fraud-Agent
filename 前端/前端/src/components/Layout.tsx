import {
  Avatar,
  Button,
  Divider,
  Hamburger,
  Image,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  mergeClasses,
  useRestoreFocusTarget,
} from "@fluentui/react-components";
import { SignOut20Regular } from "@fluentui/react-icons";
import { useEffect, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router";
import { useAuthStore } from "../stores/auth-store";
import { useStyles } from "../styles/LayoutStyle";
import NavigationView from "./NavigationView";

export default function Layout() {
  const styles = useStyles();
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);
  const alias = useAuthStore((state) => state.profileData?.alias || "用户");
  const [isNavOpen, setIsNavOpen] = useState(true);
  const restoreFocusTargetAttributes = useRestoreFocusTarget();

  const [isTopBarVisible, setIsTopBarVisible] = useState(true);
  const isNavOpenRef = useRef(isNavOpen);

  useEffect(() => {
    isNavOpenRef.current = isNavOpen;
  }, [isNavOpen]);

  function handleItemClick() {
    if (window.innerWidth <= 500) {
      setIsNavOpen(false);
    }
  }

  function handleLogoutClick() {
    logout();
    navigate("/login");
  }

  return (
    <div className={styles.root}>
      <div
        className={mergeClasses(
          styles.topBar,
          !isTopBarVisible && styles.topBarHidden
        )}>
        <Hamburger onClick={() => setIsNavOpen(!isNavOpen)} {...restoreFocusTargetAttributes} />
        <Image src="/images/JinJing.png" alt="JinJing Logo" width={16} height={16} />
        金睛
        <div className={styles.userSection}>
          {alias}
          <Menu>
            <MenuTrigger>
              <Button appearance="transparent" icon={<Avatar size={32} name={alias} image={undefined} />} />
            </MenuTrigger>
            <MenuPopover>
              <MenuList>
                <MenuItem icon={<SignOut20Regular />} onClick={handleLogoutClick}>
                  退出登录
                </MenuItem>
              </MenuList>
            </MenuPopover>
          </Menu>
        </div>
      </div>
      <div className={styles.mainContainer}>
        <NavigationView
          isOpen={isNavOpen}
          onOpenChange={setIsNavOpen}
          onItemClick={handleItemClick}
        />
        <div className={mergeClasses(styles.content, isNavOpen && styles.contentHiddenOnMobile)}>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
