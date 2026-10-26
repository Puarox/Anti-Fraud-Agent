import {
  Badge,
  Body1,
  Button,
  Card,
  CardPreview,
  Field,
  Link as FluentLink,
  Input,
  mergeClasses,
  Spinner,
  Text,
  Title1,
  tokens,
  type InputProps,
} from "@fluentui/react-components";
import { ArrowLeft16Regular, PersonCircle32Regular } from "@fluentui/react-icons";
import { Fade } from "@fluentui/react-motion-components-preview";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useTheme } from "../components/ThemeContext";
import { useAuthStore } from "../stores/auth-store";
import { useStyles } from "../styles/LoginStyle";
import api from "../utils/Api";
import { hashPassword } from "../utils/PasswordHasher";

// 登录步骤类型及常量
type LoginStep = "username" | "password" | "verification" | "reset-password";
const LOGIN_STEP = {
  USERNAME: "username" as const,
  PASSWORD: "password" as const,
  VERIFICATION: "verification" as const,
  RESET_PASSWORD: "reset-password" as const,
} as const;

async function checkUserExists(_username: string): Promise<boolean> {
  const res = await axios.get("/user/checkUserExists", {
    params: { username: _username },
  });
  if (res.data?.success && res.data?.data) {
    return true;
  }
  return false;
}

async function verifyPassword(_username: string, _password: string): Promise<boolean> {
  const hashedPwd = await hashPassword(_password);
  const loginRes = await axios.post("/user/login", {
    username: _username,
    password: hashedPwd,
  });
  // 登录成功，data为JWT令牌
  if (loginRes.data?.success && loginRes.data?.data) {
    const { setToken, setProfileData } = useAuthStore.getState();
    setToken(loginRes.data.data.token);
    try {
      const userDetailRes = await api.get("/user/getUserDetail", {
        headers: {
          token: loginRes.data.data.token,
        },
      });
      if (userDetailRes.data?.success && userDetailRes.data?.data) {
        setProfileData(userDetailRes.data.data);
      }
    } catch (error: any) {
      console.error(error.response?.data?.message || "获取用户详情失败。");
    }
    return true;
  }
  return false;
}

export default function LoginPage() {
  const styles = useStyles();
  const { isDark } = useTheme();
  const navigate = useNavigate();

  const [currentStep, setCurrentStep] = useState<LoginStep>(LOGIN_STEP.USERNAME);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // 找回密码相关状态
  const [verificationCode, setVerificationCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [recoveryContact, setRecoveryContact] = useState("");
  const [countdown, setCountdown] = useState(0);
  const [newPasswordError, setNewPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  // 识别联系方式类型
  function isEmail(val: string) {
    return /\S+@\S+\.\S+/.test(val);
  }
  function isPhone(val: string) {
    return /^1[3-9]\d{9}$/.test(val);
  }
  const isContactInfo = isEmail(username) || isPhone(username);

  // 倒计时逻辑
  useEffect(() => {
    let timer: number;
    if (countdown > 0) {
      timer = window.setInterval(() => {
        setCountdown((prev) => prev - 1);
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [countdown]);

  // 密码校验函数
  function validatePassword(pwd: string): string {
    if (pwd.length < 8) {
      return "密码至少8位";
    }
    if (!/[A-Za-z]/.test(pwd) || !/[0-9]/.test(pwd)) {
      return "密码需包含字母和数字";
    }
    return "";
  }

  // 输入处理
  function onUsernameChange(_: any, data: InputProps) {
    setUsername(data.value!);
    setError(""); // 清除错误信息
  }

  function onPasswordChange(_: any, data: InputProps) {
    setPassword(data.value!);
    setError(""); // 清除错误信息
  }

  function onNewPasswordChange(_: any, data: InputProps) {
    const val = data.value;
    setNewPassword(val!);
    setError("");
    const pwdErr = validatePassword(val!);
    setNewPasswordError(pwdErr);

    if (confirmPassword && val !== confirmPassword) {
      setConfirmPasswordError("两次输入的密码不一致");
    } else {
      setConfirmPasswordError("");
    }
  }

  function onConfirmPasswordChange(_: any, data: InputProps) {
    const val = data.value;
    setConfirmPassword(val!);
    setError("");
    if (newPassword !== val) {
      setConfirmPasswordError("两次输入的密码不一致");
    } else {
      setConfirmPasswordError("");
    }
  }

  // 提交处理
  async function handleUsernameSubmit() {
    if (!username.trim()) {
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      const userExists = await checkUserExists(username);

      if (userExists) {
        // 用户存在，切换到密码输入步骤
        setCurrentStep(LOGIN_STEP.PASSWORD);
      } else {
        // 用户不存在，显示错误信息
        setError("我们找不到这个账号。 尝试重新输入你的用户名，或注册一个新的账号。");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "发生错误，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  async function handlePasswordSubmit() {
    if (!password.trim()) {
      return;
    }
    setIsLoading(true);
    setError("");
    try {
      const isValidPassword = await verifyPassword(username, password);
      if (isValidPassword) {
        navigate("/");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "发生错误，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  function handleSendCode() {
    setCountdown(60);
    // 模拟发送验证码成功
    console.log("验证码已发送");
  }

  function handleVerificationSubmit() {
    if (!verificationCode.trim()) return;
    if (!isContactInfo && !recoveryContact.trim()) return;
    setCurrentStep(LOGIN_STEP.RESET_PASSWORD);
  }

  async function handleResetPasswordSubmit() {
    // 密码校验
    const pwdErr = validatePassword(newPassword);
    if (pwdErr) {
      setNewPasswordError(pwdErr);
      return;
    }
    if (newPassword !== confirmPassword) {
      setConfirmPasswordError("两次输入的密码不一致。");
      return;
    }

    setIsLoading(true);
    setError("");
    try {
      // 模拟重置后直接登录，用户名使用之前的或者新填入的联系方式
      const isValidPassword = await verifyPassword(username, newPassword);
      if (isValidPassword) {
        navigate("/");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "发生错误，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  // 事件处理
  function handleBack() {
    if (currentStep === LOGIN_STEP.PASSWORD) {
      setCurrentStep(LOGIN_STEP.USERNAME);
    } else if (currentStep === LOGIN_STEP.VERIFICATION) {
      setCurrentStep(LOGIN_STEP.PASSWORD);
    } else if (currentStep === LOGIN_STEP.RESET_PASSWORD) {
      setCurrentStep(LOGIN_STEP.VERIFICATION);
    }
    setError("");
  }

  function handleKeyPress(event: React.KeyboardEvent) {
    if (event.key === "Enter" && !isLoading) {
      if (currentStep === LOGIN_STEP.USERNAME) handleUsernameSubmit();
      else if (currentStep === LOGIN_STEP.PASSWORD) handlePasswordSubmit();
      else if (currentStep === LOGIN_STEP.VERIFICATION) handleVerificationSubmit();
      else if (currentStep === LOGIN_STEP.RESET_PASSWORD) handleResetPasswordSubmit();
    }
  }

  // 渲染步骤
  function renderUsernameStep() {
    return (
      <Fade visible={currentStep === LOGIN_STEP.USERNAME}>
        <Card
          className={styles.loginCard}
          style={{
            display: currentStep === LOGIN_STEP.USERNAME ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Title1 className={styles.title1}>登录</Title1>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Body1 className={styles.body}>使用您的账号</Body1>
                  <Field validationMessage={error} validationState={error ? "error" : "none"}>
                    <Input
                      maxLength={255}
                      placeholder="输入用户名、电子邮件或手机号"
                      value={username}
                      onChange={onUsernameChange}
                      onKeyUp={handleKeyPress}
                      disabled={isLoading}
                      size="large"
                    />
                  </Field>
                </div>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  className={styles.continueButton}
                  onClick={handleUsernameSubmit}
                  disabled={!username.trim() || isLoading}>
                  {isLoading ? (
                    <div className={styles.spinnerContainer}>
                      <Spinner size="tiny" />
                    </div>
                  ) : (
                    "继续"
                  )}
                </Button>

                <div style={{ textAlign: "center" }}>
                  <Text>还没有账号？</Text>
                  <FluentLink
                    onClick={() => navigate("/register")}
                    style={{
                      marginLeft: tokens.spacingHorizontalXS,
                      fontWeight: tokens.fontWeightSemibold,
                    }}>
                    立即创建一个！
                  </FluentLink>
                </div>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderPasswordStep() {
    return (
      <Fade visible={currentStep === LOGIN_STEP.PASSWORD}>
        <Card
          className={styles.loginCard}
          style={{
            display: currentStep === LOGIN_STEP.PASSWORD ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <Button
                appearance="subtle"
                icon={<ArrowLeft16Regular />}
                className={styles.backButton}
                onClick={handleBack}
                disabled={isLoading}
              />

              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Badge appearance="outline" className={styles.badge}>
                  {username}
                </Badge>
                <Title1 className={styles.title2}>输入您的密码</Title1>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Field validationMessage={error} validationState={error ? "error" : "none"}>
                    <Input
                      maxLength={50}
                      type="password"
                      placeholder="密码"
                      value={password}
                      onChange={onPasswordChange}
                      onKeyUp={handleKeyPress}
                      disabled={isLoading}
                      size="large"
                    />
                  </Field>
                </div>{" "}
                <div className={styles.linkSection}>
                  <FluentLink className={styles.link} onClick={() => setCurrentStep(LOGIN_STEP.VERIFICATION)}>
                    忘记了密码？
                  </FluentLink>
                </div>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  className={styles.continueButton}
                  onClick={handlePasswordSubmit}
                  disabled={!password.trim() || isLoading}>
                  {isLoading ? (
                    <div className={styles.spinnerContainer}>
                      <Spinner size="tiny" />
                    </div>
                  ) : (
                    "登录"
                  )}
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderVerificationStep() {
    return (
      <Fade visible={currentStep === LOGIN_STEP.VERIFICATION}>
        <Card
          className={styles.loginCard}
          style={{
            display: currentStep === LOGIN_STEP.VERIFICATION ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <Button
                appearance="subtle"
                icon={<ArrowLeft16Regular />}
                className={styles.backButton}
                onClick={handleBack}
                disabled={isLoading}
              />

              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Badge appearance="outline" className={styles.badge}>
                  {username}
                </Badge>
                <Title1 className={styles.title2}>重置密码</Title1>
              </div>

              <div className={styles.inputSection}>
                {!isContactInfo && (
                  <div className={styles.inputGroup}>
                    <Field label="请输入注册时绑定的邮箱或手机号">
                      <Input
                        placeholder="邮箱或手机号"
                        value={recoveryContact}
                        onChange={(_, data) => setRecoveryContact(data.value)}
                        size="large"
                      />
                    </Field>
                  </div>
                )}

                <div className={styles.verificationGroup}>
                  <Input
                    className={styles.verificationInput}
                    placeholder="验证码"
                    value={verificationCode}
                    onChange={(_, data) => setVerificationCode(data.value)}
                    size="large"
                  />
                  <Button
                    className={styles.sendCodeButton}
                    disabled={countdown > 0}
                    onClick={handleSendCode}
                    appearance="primary"
                    size="large">
                    {countdown > 0 ? `${countdown}s` : "发送验证码"}
                  </Button>
                </div>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  onClick={handleVerificationSubmit}
                  disabled={!verificationCode.trim() || (!isContactInfo && !recoveryContact.trim())}>
                  继续
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderResetPasswordStep() {
    return (
      <Fade visible={currentStep === LOGIN_STEP.RESET_PASSWORD}>
        <Card
          className={styles.loginCard}
          style={{
            display: currentStep === LOGIN_STEP.RESET_PASSWORD ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <Button
                appearance="subtle"
                icon={<ArrowLeft16Regular />}
                className={styles.backButton}
                onClick={handleBack}
                disabled={isLoading}
              />

              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Title1 className={styles.title2}>设置新密码</Title1>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Field
                    label="新密码"
                    validationMessage={newPasswordError || (error && !confirmPasswordError ? error : "")}
                    validationState={newPasswordError || (error && !confirmPasswordError) ? "error" : "none"}>
                    <Input
                      maxLength={50}
                      type="password"
                      placeholder="新密码"
                      value={newPassword}
                      onChange={onNewPasswordChange}
                      size="large"
                    />
                  </Field>
                  <Field
                    label="确认密码"
                    validationMessage={confirmPasswordError}
                    validationState={confirmPasswordError ? "error" : "none"}>
                    <Input
                      maxLength={50}
                      type="password"
                      placeholder="确认密码"
                      value={confirmPassword}
                      onChange={onConfirmPasswordChange}
                      onKeyUp={handleKeyPress}
                      size="large"
                    />
                  </Field>
                </div>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  onClick={handleResetPasswordSubmit}
                  disabled={
                    !newPassword.trim() ||
                    !confirmPassword.trim() ||
                    !!newPasswordError ||
                    !!confirmPasswordError ||
                    isLoading
                  }>
                  {isLoading ? <Spinner size="tiny" /> : "登录"}
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  return (
    <div className={mergeClasses(styles.container, isDark ? styles.containerDark : styles.containerLight)}>
      <div className={styles.cardContainer}>
        {renderUsernameStep()}
        {renderPasswordStep()}
        {renderVerificationStep()}
        {renderResetPasswordStep()}
      </div>
    </div>
  );
}
