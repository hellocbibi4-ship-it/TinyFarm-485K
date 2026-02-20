import requests
import pytest

# Compilation:
# pytest Project/src/test/java/com/farm/tinyfarm/Services/FermeServiceTest.py -qsv

BASE_URL = "http://localhost:8080/api/fermes"

#Fonction qui permet de créer une ferme et de la supprimer à la fin du test.
@pytest.fixture
def new_ferme():
    # SETUP
    response = requests.post(BASE_URL, json={"nom": "Ferme_Test"})
    ferme = response.json()
    yield ferme  # Le test s'exécute ici
    # TEARDOWN
    requests.delete(f"{BASE_URL}/{ferme['idFerme']}")

def test_create_ferme():
    payload = {"nom": "Ferme485K"}
    response = requests.post(BASE_URL, json=payload)
    
    assert response.status_code == 201
    data = response.json()
    print(data)
    assert data['soldeEcus'] == 1500, "Le solde écus n'est pas égal à 1500"
    assert data['score'] == 0, "Le score n'est pas égal à 0"
    assert data['nom'] == "Ferme485K", "Le nom de la ferme n'est pas 'Ferme485K'"
    requests.delete(f"{BASE_URL}/{data['idFerme']}")

def test_ajouter_ecus(new_ferme):
    fermeId = new_ferme.get("idFerme")

    montant = 500
    patch_url = f"{BASE_URL}/{fermeId}/ajout-ecus"
    params = {"montant": montant}
    response = requests.patch(patch_url, params=params)

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 2000
    print(data)

def test_retirer_ecus(new_ferme):
    fermeId = new_ferme.get("idFerme")

    montant = 500
    patch_url = f"{BASE_URL}/{fermeId}/retirer-ecus"
    params = {"montant": montant}
    response = requests.patch(patch_url, params=params)

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 1000
    print(data)

def test_ajouter_score(new_ferme):
    fermeId=new_ferme["idFerme"]

    montant = 100;
    patch_url=f"{BASE_URL}/{fermeId}/score"
    params = {"montant": montant}
    response = requests.patch(patch_url, params=params)

    data = response.json()
    assert response.status_code == 200
    assert data.get("score") == 100
    print(data)