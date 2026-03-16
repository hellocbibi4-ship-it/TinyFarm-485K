const loginBtn = document.getElementById("login-btn");
const loginScreen = document.getElementById("login-screen");
const clsBtn = document.getElementById("Trophy");
const farmScreen = document.getElementById("farm-screen");
const classementScreen = document.getElementById("Classement");

loginBtn.addEventListener("click", () => {
  // cacher le login
  loginScreen.classList.add("hidden");

  // afficher la ferme
  farmScreen.classList.remove("hidden");


});
 classement();
clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show");
    clsBtn.classList.toggle("trophy2");

    if (classementScreen.classList.contains("show")) {
        classement(); // met à jour le tableau si nécessaire
    }
});
const tbody = document.getElementById("classement-body");




async function classement() {
  const response = await fetch('./data/farmData.json');
  const data = await response.json();
  tbody.innerHTML = '';
  if (data.players) {
    data.players.forEach((p, i) => {
      tbody.innerHTML += `
        <tr>
          <td>${i+1}</td>
          <td>${p.name}</td>
          <td>${p.production}</td>
          <td>${p.capacity}</td>
          <td>${p.money}</td>
        </tr>
      `;
});
}

}

document.addEventListener("DOMContentLoaded", () => {

    const settingsButtons = document.querySelectorAll(".settings-btn");
    const settingsPanels = document.querySelectorAll(".settings-panel");

    settingsButtons.forEach((btn, index) => {

        btn.addEventListener("click", (e) => {
            e.stopPropagation();

            const isOpen = btn.classList.contains("open");

            // Ferme tout
            settingsButtons.forEach(b => b.classList.remove("open"));
            settingsPanels.forEach(p => p.classList.remove("open"));

            // Si ce n'était PAS déjà ouvert → on ouvre
            if (!isOpen) {
                btn.classList.add("open");
                settingsPanels[index].classList.add("open");
            }
        });

    });

});

document.querySelectorAll(".language-btn").forEach(btn => {

    btn.addEventListener("click", (e) => {
        e.stopPropagation();

        const panel = btn.nextElementSibling;
        panel.classList.toggle("open");
    });

});

document.querySelectorAll(".logout-btn").forEach(btn => {

    btn.addEventListener("click", () => {

        const loginScreen = document.getElementById("login-screen");
        const farmScreen = document.getElementById("farm-screen");

        // Retour au login
        farmScreen.classList.add("hidden");
        loginScreen.classList.remove("hidden");

        // Fermer tous les menus settings
        document.querySelectorAll(".settings-btn").forEach(b => b.classList.remove("open"));
        document.querySelectorAll(".settings-panel").forEach(p => p.classList.remove("open"));

    });

});