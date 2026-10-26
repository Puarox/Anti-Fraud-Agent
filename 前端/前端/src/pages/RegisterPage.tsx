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
  Select,
  Spinner,
  Text,
  Title1,
  tokens,
  type InputProps,
} from "@fluentui/react-components";
import { ArrowLeft16Regular, PersonCircle32Regular } from "@fluentui/react-icons";
import { Fade } from "@fluentui/react-motion-components-preview";
import axios from "axios";
import React, { useState } from "react";
import { useNavigate } from "react-router";
import { useTheme } from "../components/ThemeContext";
import { AreaCodes } from "../models/AreaCode";
import { useStyles } from "../styles/RegisterStyle";
import { hashPassword } from "../utils/PasswordHasher";

type RegisterStep = "username" | "password" | "alias" | "phone" | "done";

const REGISTER_STEP = {
  USERNAME: "username" as const,
  PASSWORD: "password" as const,
  ALIAS: "alias" as const,
  PHONE: "phone" as const,
  DONE: "done" as const,
} as const;

async function checkUserExists(username: string): Promise<boolean> {
  try {
    const res = await axios.get("/user/checkUserExists", {
      params: {
        username: username,
      },
    });
    // 根据实际 API 响应格式调整
    if (res.data?.success) {
      return res.data.data;
    }
    return true;
  } catch (error: any) {
    // 如果接口返回错误，认为用户不存在（用于注册场景）
    if (error.response?.status === 404) {
      return true;
    }
    throw error;
  }
}

async function registerUser(userData: {
  username: string;
  password: string;
  email?: string;
  phone?: string;
  alias: string;
  avatarUrl?: string;
}): Promise<boolean> {
  try {
    const res = await axios.post("/user/register", userData);
    return res.data?.success === true;
  } catch (error: any) {
    throw error;
  }
}

export default function Register() {
  const styles = useStyles();
  const { isDark } = useTheme();
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState<RegisterStep>(REGISTER_STEP.USERNAME);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [alias, setAlias] = useState("");
  const [areaCode, setAreaCode] = useState("+86");
  const [phone, setPhone] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [confirmPasswordError, setConfirmPasswordError] = useState("");

  // 输入处理
  function onUsernameChange(_: React.FormEvent<HTMLInputElement>, data: InputProps) {
    setUsername(data.value!);
    setError(""); // 清除错误信息
  }

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

  // 密码输入处理
  function onPasswordChange(_: React.FormEvent<HTMLInputElement>, data: InputProps) {
    setPassword(data.value!);
    setError(""); // 清除通用错误信息
    const pwdErr = validatePassword(data.value!);
    setPasswordError(pwdErr);
    // 校验确认密码一致性
    if (confirmPassword && data.value !== confirmPassword) {
      setConfirmPasswordError("两次输入的密码不一致");
    } else {
      setConfirmPasswordError("");
    }
  }

  // 确认密码输入处理
  function onConfirmPasswordChange(_: React.FormEvent<HTMLInputElement>, data: InputProps) {
    setConfirmPassword(data.value!);
    setError(""); // 清除通用错误信息
    if (password !== data.value) {
      setConfirmPasswordError("两次输入的密码不一致");
    } else {
      setConfirmPasswordError("");
    }
  }

  function onAliasChange(_: React.FormEvent<HTMLInputElement>, data: InputProps) {
    setAlias(data.value!);
    setError("");
  }

  function onPhoneChange(_: React.FormEvent<HTMLInputElement>, data: InputProps) {
    setPhone(data.value!);
    setError("");
  }

  function handleKeyPress(event: React.KeyboardEvent): void {
    if (event.key === "Enter" && !isLoading) {
      switch (currentStep) {
        case REGISTER_STEP.USERNAME:
          handleUsernameSubmit();
          break;
        case REGISTER_STEP.PASSWORD:
          handlePasswordSubmit();
          break;
        case REGISTER_STEP.ALIAS:
          setCurrentStep(REGISTER_STEP.PHONE);
          break;
        case REGISTER_STEP.PHONE:
          handlePhoneSubmit();
          break;
        default:
          break;
      }
    }
  }

  function handleBack() {
    setCurrentStep((prev) => {
      switch (prev) {
        case REGISTER_STEP.PASSWORD:
          return REGISTER_STEP.USERNAME;
        case REGISTER_STEP.ALIAS:
          return REGISTER_STEP.PASSWORD;
        case REGISTER_STEP.PHONE:
          return REGISTER_STEP.ALIAS;
        default:
          return prev;
      }
    });
  }

  function handlePasswordSubmit() {
    // 密码校验
    const pwdErr = validatePassword(password);
    if (pwdErr) {
      setPasswordError(pwdErr);
      return;
    }
    if (password !== confirmPassword) {
      setConfirmPasswordError("两次输入的密码不一致。");
      return;
    }
    setPasswordError("");
    setConfirmPasswordError("");
    setError("");
    setCurrentStep(REGISTER_STEP.ALIAS);
  }

  async function handleUsernameSubmit() {
    if (!username) {
      setError("用户名不能为空。");
      return;
    }
    setIsLoading(true);
    try {
      const exists = await checkUserExists(username);
      if (exists) {
        setError("用户名已存在，请选择其他用户名。");
      } else {
        setError("");
        setCurrentStep(REGISTER_STEP.PASSWORD);
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "检查用户名时出错，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  async function handlePhoneSubmit() {
    if (!phone.trim()) {
      setError("请输入手机号。");
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      const hashedPwd = await hashPassword(password);
      const userData = {
        username: username,
        password: hashedPwd,
        email: undefined,
        phone: `${areaCode} ${phone}`,
        alias: alias,
        avatarUrl: undefined,
      };

      const success = await registerUser(userData);

      if (success) {
        setCurrentStep(REGISTER_STEP.DONE);
      } else {
        setError("注册失败，请稍后重试。");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "发生错误，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSkipPhone() {
    setIsLoading(true);
    setError("");

    try {
      const hashedPwd = await hashPassword(password);
      const userData = {
        username: username,
        password: hashedPwd,
        email: undefined,
        phone: undefined,
        alias: alias,
        avatarUrl: undefined,
      };

      const success = await registerUser(userData);

      if (success) {
        setCurrentStep(REGISTER_STEP.DONE);
      } else {
        setError("注册失败，请稍后重试。");
      }
    } catch (error: any) {
      setError(error.response?.data?.message || "发生错误，请稍后再试。");
    } finally {
      setIsLoading(false);
    }
  }

  function renderUsernameStep() {
    return (
      <Fade visible={currentStep === REGISTER_STEP.USERNAME}>
        <Card
          className={styles.registerCard}
          style={{
            display: currentStep === REGISTER_STEP.USERNAME ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Title1 className={styles.title1}>注册您的账号</Title1>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Body1 className={styles.body}>输入您的用户名</Body1>
                  <Field validationMessage={error} validationState={error ? "error" : "none"}>
                    <Input
                      maxLength={50}
                      placeholder="输入用户名"
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
                  <Text>已经有一个账号？</Text>
                  <FluentLink
                    onClick={() => navigate("/login")}
                    style={{
                      marginLeft: tokens.spacingHorizontalXS,
                      fontWeight: tokens.fontWeightSemibold,
                    }}>
                    登录
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
      <Fade visible={currentStep === REGISTER_STEP.PASSWORD}>
        <Card
          className={styles.registerCard}
          style={{
            display: currentStep === REGISTER_STEP.PASSWORD ? "block" : "none",
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
                  <Field
                    label="密码"
                    validationMessage={passwordError}
                    validationState={passwordError ? "error" : "none"}>
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
                  <Field
                    label="确认密码"
                    validationMessage={confirmPasswordError}
                    validationState={confirmPasswordError ? "error" : "none"}>
                    <Input
                      type="password"
                      placeholder="确认密码"
                      value={confirmPassword}
                      onChange={onConfirmPasswordChange}
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
                  onClick={handlePasswordSubmit}
                  disabled={
                    !password.trim() ||
                    !confirmPassword.trim() ||
                    !!passwordError ||
                    !!confirmPasswordError ||
                    isLoading
                  }>
                  {isLoading ? (
                    <div className={styles.spinnerContainer}>
                      <Spinner size="tiny" />
                    </div>
                  ) : (
                    "继续"
                  )}
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderAliasStep() {
    return (
      <Fade visible={currentStep === REGISTER_STEP.ALIAS}>
        <Card
          className={styles.registerCard}
          style={{
            display: currentStep === REGISTER_STEP.ALIAS ? "block" : "none",
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
                <Title1 className={styles.title2}>设置您的昵称</Title1>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Field label="昵称">
                    <Input
                      maxLength={50}
                      placeholder="输入昵称"
                      value={alias}
                      onChange={onAliasChange}
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
                  onClick={() => setCurrentStep(REGISTER_STEP.PHONE)}
                  disabled={!alias.trim() || isLoading}>
                  继续
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderPhoneStep() {
    return (
      <Fade visible={currentStep === REGISTER_STEP.PHONE}>
        <Card
          className={styles.registerCard}
          style={{
            display: currentStep === REGISTER_STEP.PHONE ? "block" : "none",
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
                <Title1 className={styles.title2}>验证手机号码</Title1>
              </div>

              {/* 视频组件 */}
              <div className={styles.videoContainer}>
                <video className={styles.video} autoPlay loop muted playsInline>
                  <source src="/images/register_biometrics.mp4" type="video/mp4" />
                  您的浏览器不支持视频播放。
                </video>
              </div>

              <div className={styles.inputSection}>
                <div className={styles.inputGroup}>
                  <Body1 className={styles.body}>输入您的手机号码</Body1>
                  <Field label="手机号码" validationMessage={error} validationState={error ? "error" : "none"}>
                    <div className={styles.phoneInputContainer}>
                      <Select
                        className={styles.areaCodeSelect}
                        onChange={(_, data) => {
                          setAreaCode(data.value);
                        }}
                        value={areaCode}
                        disabled={isLoading}>
                        {AreaCodes.map((code) => (
                          <option key={code.key} value={code.key}>
                            {code.text}
                          </option>
                        ))}
                      </Select>

                      <Input
                        maxLength={11}
                        className={styles.phoneInput}
                        placeholder="输入手机号"
                        value={phone}
                        onChange={onPhoneChange}
                        onKeyUp={handleKeyPress}
                        disabled={isLoading}
                      />
                    </div>
                  </Field>
                </div>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  className={styles.continueButton}
                  onClick={handlePhoneSubmit}
                  disabled={!phone.trim() || isLoading}>
                  {isLoading ? (
                    <div className={styles.spinnerContainer}>
                      <Spinner size="tiny" />
                    </div>
                  ) : (
                    "继续"
                  )}
                </Button>

                <Button size="large" className={styles.skipButton} onClick={handleSkipPhone} disabled={isLoading}>
                  跳过
                </Button>
              </div>
            </div>
          </CardPreview>
        </Card>
      </Fade>
    );
  }

  function renderDoneStep() {
    return (
      <Fade visible={currentStep === REGISTER_STEP.DONE}>
        <Card
          className={styles.registerCard}
          style={{
            display: currentStep === REGISTER_STEP.DONE ? "block" : "none",
          }}>
          <CardPreview>
            <div className={styles.cardContent}>
              <div className={styles.header}>
                <PersonCircle32Regular className={styles.logo} />
                <Title1 className={styles.title1}>注册成功！</Title1>
                <Body1 className={styles.body}>请等待管理员审核。</Body1>
              </div>

              <div className={styles.actionSection}>
                <Button
                  appearance="primary"
                  size="large"
                  className={styles.continueButton}
                  onClick={() => navigate("/login")}>
                  返回登录
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
        {renderAliasStep()}
        {renderPhoneStep()}
        {renderDoneStep()}
      </div>
    </div>
  );
}
