const backendUrl = "http://localhost:8081";
const frontendRedirectUri = "http://localhost:5173/callback";

// 🔁 Отримуємо code з URL (після редиректу з Keycloak)
const authCode = new URL(window.location.href).searchParams.get("code");

document.addEventListener("DOMContentLoaded", () => {
  const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const getDataBtn = document.getElementById("getDataBtn");

  loginBtn.addEventListener("click", handleLogin);
  logoutBtn.addEventListener("click", handleLogout);
  getDataBtn.addEventListener("click", fetchUserData);

  // Якщо є "code" у URL — обмінюємо його на токен
  if (authCode) {
    exchangeAuthCode(authCode);
  } else {
    renderUI();
  }
});

// 🔐 1. Вхід через редирект на Keycloak
function handleLogin() {
  window.location.href = `${backendUrl}/api/v1/auth/login?redirect_uri=${frontendRedirectUri}`;
}

function handleLogout() {
  const token = localStorage.getItem("access_token");
  const idToken = localStorage.getItem("id_token");
  const logoutRedirect = "http://localhost:5173";

  if (!token || !idToken) {
    window.location.href = `${backendUrl}/api/v1/auth/logout?redirect_uri=${logoutRedirect}`;
    return;
  }

  fetch(
    `${backendUrl}/api/v1/auth/logout?redirect_uri=${logoutRedirect}&id_token_hint=${idToken}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  ).finally(() => {
    localStorage.removeItem("access_token");
    localStorage.removeItem("id_token");
    window.location.href = logoutRedirect;
  });
}

// 🔁 3. Обмін коду авторизації на access_token
function exchangeAuthCode(code) {
  fetch(`${backendUrl}/api/v1/auth/code`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      code,
      redirect_uri: frontendRedirectUri,
    }),
  })
    .then((res) => {
      if (!res.ok) throw new Error("❌ Не вдалося обміняти code");
      return res.json();
    })
    .then((data) => {
      console.log(data);

      localStorage.setItem("access_token", data.access_token);
      localStorage.setItem("id_token", data.id_token);
      window.history.replaceState({}, document.title, "/"); // Видаляємо ?code= з URL
      renderUI();
    })
    .catch((err) => {
      console.error("❌ Помилка обміну коду:", err);
      renderUI();
    });
}

// 👤 4. Отримання даних користувача
function fetchUserData() {
  const token = localStorage.getItem("access_token");

  if (!token) {
    alert("⚠️ Токен не знайдено. Увійдіть ще раз.");
    return;
  }

  fetch(`${backendUrl}/api/v1/user/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
    .then((res) => {
      if (!res.ok) throw new Error("❌ Невалідний токен або сесія завершена");
      return res.json();
    })
    .then((user) => {
      document.getElementById(
        "userData"
      ).innerText = `👤 Вітаємо, ${user.name} (${user.email})`;
    })
    .catch((err) => {
      console.error("❌ Помилка при отриманні користувача:", err);
      localStorage.removeItem("access_token");
      window.location.reload();
    });
}

// 🎨 5. Відображення інтерфейсу на основі наявності токена
function renderUI() {
  const token = localStorage.getItem("access_token");

  const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const getDataBtn = document.getElementById("getDataBtn");
  const userData = document.getElementById("userData");

  if (token) {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "inline-block";
    getDataBtn.style.display = "inline-block";
  } else {
    loginBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    getDataBtn.style.display = "none";
    userData.innerText = "";
  }
}
