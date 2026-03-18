# Guide rapide - Reutiliser la popup "Achat animaux"

Objectif: integrer la popup dans la page principale sans changer le fond de cette page.

## 1) Fichiers a reutiliser

- `css/achat-animaux.css`
- `js/achat-animaux.js`
- `assets/vache.svg`, `assets/poule.svg`, `assets/lapin.svg`

## 2) Dans la page principale (HTML)

1. Garde ton fond actuel (ne pas copier `div.fond-flou`).
2. Copie uniquement le bloc popup + boutons + message depuis `achat-animaux.html`:
   - `section#popup-achat`
   - `button#bouton-ouvrir`
   - `section.resume-achats`
   - `p#message-action`
3. Ajoute ton bouton d'entree (si tu en as deja un, garde son id):

```html
<button id="btn-achat-accueil" type="button">Achat animaux</button>
```

4. Charge les fichiers CSS/JS:

```html
<link rel="stylesheet" href="css/achat-animaux.css">
<script src="js/achat-animaux.js"></script>
```

## 3) Liaison avec un bouton de la page principale (JS)

Ajoute ce petit pont dans ton script de page principale (ou en bas de la page):

```js
const boutonAccueilAchat = document.getElementById("btn-achat-accueil");
const popupAchat = document.getElementById("popup-achat");
const boutonOuvrirInterne = document.getElementById("bouton-ouvrir");

if (boutonAccueilAchat && popupAchat && boutonOuvrirInterne) {
    boutonAccueilAchat.addEventListener("click", () => {
        popupAchat.classList.remove("cache");
        boutonOuvrirInterne.classList.add("cache");
    });
}
```

## 4) Garder un fond different

Le style de la popup et le fond de ta page sont separes.

- Pour conserver ton fond principal: ne mets pas `div.fond-flou` dans la page.
- Si besoin, ajoute cette surcharge CSS dans la page principale:

```css
.fond-flou {
    display: none;
}
```

## 5) Attention importante

Le script `js/achat-animaux.js` lit ces ids: 

- `popup-achat`, `bouton-fermer`, `bouton-ouvrir`
- `solde-ecus`, `message-action`
- `compteur-vache`, `compteur-poule`, `compteur-lapin`

Ne les renomme pas, sinon il faudra adapter le JS.
