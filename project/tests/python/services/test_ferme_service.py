# Module de tests Pytest pour le service ferme de TinyFarm.

import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_ferme_service.py -qsv

BASE_URL = "http://localhost:8080/api/fermes"


# Fonction qui permet de creer une ferme et de la supprimer a la fin du test.
@pytest.fixture
def new_ferme():
    # SETUP
    response = requests.post(BASE_URL, json={"nom": "Ferme_Test"})
    ferme = response.json()
    yield ferme
    # TEARDOWN
    requests.delete(f"{BASE_URL}/{ferme['idFerme']}")


def test_create_ferme():
    payload = {"nom": "Ferme485K"}
    response = requests.post(BASE_URL, json=payload)

    assert response.status_code == 201
    data = response.json()
    print(data)
    assert data["soldeEcus"] == 1500, "Le solde ecus n'est pas egal a 1500"
    assert data["score"] == 0, "Le score n'est pas egal a 0"
    assert data["nom"] == "Ferme485K", "Le nom de la ferme n'est pas 'Ferme485K'"
    requests.delete(f"{BASE_URL}/{data['idFerme']}")


def test_create_ferme_nom_invalide_court():
    # Nom de moins de 3 caracteres : refuse
    response = requests.post(BASE_URL, json={"nom": "ab"})
    assert response.status_code == 400


def test_create_ferme_nom_invalide_long():
    # Plus de 16 caracteres : refuse
    response = requests.post(BASE_URL, json={"nom": "a" * 17})
    assert response.status_code == 400


def test_create_ferme_nom_invalide_caracteres():
    response = requests.post(BASE_URL, json={"nom": "ferme!!"})
    assert response.status_code == 400


def test_ajouter_ecus(new_ferme):
    ferme_id = new_ferme.get("idFerme")

    patch_url = f"{BASE_URL}/{ferme_id}/ajout-ecus"
    response = requests.patch(patch_url, params={"montant": 500})

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 2000
    print(data)


def test_retirer_ecus(new_ferme):
    ferme_id = new_ferme.get("idFerme")

    patch_url = f"{BASE_URL}/{ferme_id}/retirer-ecus"
    response = requests.patch(patch_url, params={"montant": 500})

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 1000
    print(data)


def test_ajouter_score(new_ferme):
    ferme_id = new_ferme["idFerme"]

    patch_url = f"{BASE_URL}/{ferme_id}/score"
    response = requests.patch(patch_url, params={"montant": 100})

    data = response.json()
    assert response.status_code == 200
    assert data.get("score") == 100
    print(data)


def test_front_data(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.get(f"{BASE_URL}/{ferme_id}/front-data")

    assert response.status_code == 200
    data = response.json()
    assert data["farmId"] == ferme_id
    assert data["cash"] == 1500
    assert data["score"] == 0
    assert data["gameTime"]["day"] == 1
    assert data["communityPurchases"]["remaining"] == 12
    assert data["communityPurchases"]["maxPerDay"] == 12
    assert isinstance(data["animals"], list)
    assert isinstance(data["stockInventory"], list)


def test_hibernation(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.patch(
        f"{BASE_URL}/{ferme_id}/hibernation", params={"etat": "true"}
    )
    assert response.status_code == 200
    assert "true" in response.text


def test_classement():
    response = requests.get(f"{BASE_URL}/classement")
    assert response.status_code == 200
    data = response.json()
    assert "ranking" in data
    assert isinstance(data["ranking"], list)


def test_classement_update():
    response = requests.get(f"{BASE_URL}/classement/update")
    assert response.status_code == 200
    data = response.json()
    assert "ranking" in data


def test_supprimer_ferme():
    # Creation
    response = requests.post(BASE_URL, json={"nom": "Ferme_A_Supp"})
    assert response.status_code == 201
    ferme_id = response.json()["idFerme"]

    # Suppression
    delete_response = requests.delete(f"{BASE_URL}/{ferme_id}")
    assert delete_response.status_code == 204


def test_acheter_objet_entretien_eau(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.post(
        f"{BASE_URL}/{ferme_id}/acheter-objet-entretien", params={"type": "EAU"}
    )
    assert response.status_code == 200
    data = response.json()
    # Apres achat d'un seau d'eau (2 ecus), le solde doit avoir baisse.
    assert data["cash"] == 1498
    assert data["careInventory"]["water-bucket"] == 1


def test_passer_jour(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.post(f"{BASE_URL}/{ferme_id}/passer-jour")
    assert response.status_code == 200
    data = response.json()
    assert data["gameTime"]["day"] == 2
    # Le quota de la collectivite est remis a 12 chaque nouveau jour.
    assert data["communityPurchases"]["remaining"] == 12