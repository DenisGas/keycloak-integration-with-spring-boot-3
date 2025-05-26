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
  getDataBtn.addEventListener("click", fullLogout);


  // Якщо є "code" у URL — обмінюємо його на токен
  if (authCode) {
    exchangeAuthCode(authCode);
  } else {
    renderUI();
  }
});

function handleLogin(forceLogin = false) {
  const flag = localStorage.getItem("is_fullLogout");

  console.log(flag)
  if (flag === "true") {
    localStorage.setItem("is_fullLogout", "false");
    forceLogin = true;
  }

  const url = new URL(`${backendUrl}/api/v1/auth/login`);
  url.searchParams.set("redirect_uri", frontendRedirectUri);

  if (forceLogin === true) {
    url.searchParams.set("prompt", "true");
  }

  window.location.href = url.toString();
}

function fullLogout() {
 localStorage.setItem("is_fullLogout", true);
 handleLogout();
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
      redirectUri: frontendRedirectUri, // ИСПРАВЛЕНО: было redirect_uri, стало redirectUri
    }),
  })
    .then((res) => {
      if (!res.ok) {
        return res.json().then(errorData => {
          console.error("❌ Ошибка сервера:", errorData);
          throw new Error(`❌ Не вдалося обміняти code: ${errorData.error || res.statusText}`);
        });
      }
      return res.json();
    })
    .then((data) => {
      console.log("✅ Токены получены:", data);

      // Сохраняем токены с правильными именами
      localStorage.setItem("access_token", data.access_token);
      if (data.id_token) {
        localStorage.setItem("id_token", data.id_token);
      }

      window.history.replaceState({}, document.title, "/"); // Видаляємо ?code= з URL
      renderUI();
    })
    .catch((err) => {
      console.error("❌ Помилка обміну коду:", err);
      // Показываем пользователю более подробную ошибку
      alert(`Ошибка авторизации: ${err.message}`);
      renderUI();
    });
}

function renderUI() {
  const token = localStorage.getItem("access_token");
  const idToken = localStorage.getItem("id_token");

  const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const getDataBtn = document.getElementById("getDataBtn");

  const hiBlock = document.getElementById("hi");
  const tokenBlock = document.getElementById("tokenBlock");
  const tokenValue = document.getElementById("tokenValue");

  if (token) {
    loginBtn.style.display = "none";
    logoutBtn.style.display = "inline-block";
    getDataBtn.style.display = "inline-block";

    hiBlock.style.display = "block";
    tokenBlock.style.display = "block";
    tokenValue.textContent = token;

    fetch(`${backendUrl}/api/v1/user/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("❌ Сесія недійсна");
        return res.json();
      })
      .then((d) => {
        let user = d.data;
        document.getElementById("username").textContent = " " + user.name;
        document.getElementById("email").textContent = user.email;
      })
      .catch((err) => {
        console.error("❌ Помилка при отриманні даних:", err);
        localStorage.removeItem("access_token");
        localStorage.removeItem("id_token");
        hiBlock.style.display = "none";
        tokenBlock.style.display = "none";
        loginBtn.style.display = "inline-block";
        logoutBtn.style.display = "none";
        getDataBtn.style.display = "none";
      });
  } else {
    loginBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    getDataBtn.style.display = "none";
    hiBlock.style.display = "none";
    tokenBlock.style.display = "none";
    tokenValue.textContent = "";
  }
}

document.getElementById("copyTokenBtn").addEventListener("click", () => {
  const token = document.getElementById("tokenValue").textContent;
  navigator.clipboard.writeText(token).then(() => {
    alert("✅ Токен скопійовано!");
  });
});