const cowCountInput = document.getElementById("cow-count-input");
const cowButtonsContainer = document.getElementById("cow-buttons-container");
const COW_COUNT_KEY = "tinyfarmCowCount";

function createCowButton(index) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "cow-btn";
  button.textContent = `Vache ${index}`;
  return button;
}

function getStoredCowCount() {
  const rawCount = localStorage.getItem(COW_COUNT_KEY);
  return Math.max(0, Number.parseInt(rawCount, 10) || 0);
}

function setStoredCowCount(count) {
  localStorage.setItem(COW_COUNT_KEY, String(count));
}

function setCowButtonCount(count) {
  const targetCount = Math.max(0, Number.parseInt(count, 10) || 0);
  let currentCount = cowButtonsContainer.children.length;

  while (currentCount < targetCount) {
    currentCount += 1;
    cowButtonsContainer.appendChild(createCowButton(currentCount));
  }

  while (currentCount > targetCount) {
    cowButtonsContainer.lastElementChild.remove();
    currentCount -= 1;
  }

  setStoredCowCount(targetCount);
}

cowCountInput.addEventListener("input", (event) => {
  setCowButtonCount(event.target.value);
});

const initialCowCount = getStoredCowCount();
cowCountInput.value = String(initialCowCount);
setCowButtonCount(initialCowCount);

window.setCowButtonCount = setCowButtonCount;
