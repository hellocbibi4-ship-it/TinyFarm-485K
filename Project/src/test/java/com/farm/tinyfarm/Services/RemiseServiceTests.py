
import requests
import pytest

# Compilation:
# pytest Project/src/test/java/com/farm/tinyfarm/Services/RemiseServiceTests.py -qsv

BASE_URL = "http://localhost:8080/api/remise"
URL_FERME = "http://localhost:8080/api/fermes"

#Fonction qui permet de créer une ferme et de la supprimer à la fin du test.
@pytest.fixture
def new_remise():
    # SETUP
    response = requests.post(URL_FERME, json={"nom": "Ferme_Test"})
    assert(response.status_code == 201)
    ferme_data = response.json()
    fid = ferme_data.get("idFerme")

    res_remise = requests.get(f"{BASE_URL}/{fid}")
    assert(res_remise.status_code == 200)
    remise = res_remise.json();

    yield remise  # Le test s'exécute ici
    # TEARDOWN
    requests.delete(f"{URL_FERME}/{fid}")

def test_create_remise() :
    payload = {"nom": "Ferme485K"}
    response = requests.post(URL_FERME, json=payload)
    
    assert response.status_code == 201
    ferme_data = response.json()
    fid = ferme_data.get("idFerme")

    res_remise = requests.get(f"{BASE_URL}/{fid}")

    assert res_remise.status_code == 200
    remise_data = res_remise.json()

    print(remise_data)
    assert(remise_data["remiseId"] == fid)
    requests.delete(f"{URL_FERME}/{ferme_data['idFerme']}")

def test_ajouter_stock(new_remise) :
    fermeId = new_remise.get("remiseId")

    patch_url = f"{BASE_URL}/{fermeId}/ajouter-stock"
    params = {"montant": 2,"stock": "PAILLE"}
    
    response = requests.patch(patch_url, params= params)
    if response.status_code == 400:
        print("\nMESSAGE DU SERVEUR :", response.text)
    assert(response.status_code == 200)
    
    ferme_data = response.json()
    assert(ferme_data.get("stockPaille") == 2)
    print(ferme_data)


