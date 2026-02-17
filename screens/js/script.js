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
