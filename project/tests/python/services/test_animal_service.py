import requests
import pytest
import time

# Lancement:
# pytest project/tests/python/services/test_animal_service.py -qsv

BASE_URL = "http://localhost:8080/api/animaux"
URL_FERME = "http://localhost:8080/api/fermes"


def test_traire_vache():
    # Creer une ferme de test
    ferme_payload = {"nom": f"FT{int(time.time()) % 1000000}"}
    ferme_response = requests.post(URL_FERME, json=ferme_payload)
    assert ferme_response.status_code == 201
    ferme_id = ferme_response.json().get("idFerme")
    assert ferme_id is not None

    # Creer une vache de test dans la ferme
    payload = {
        "nom": "VacheTest01",
        "typeAnimal": "VACHE",
        "stockLaitPis": 10
    }
    response = requests.post(f"{URL_FERME}/{ferme_id}/animaux", json=payload)
    assert response.status_code == 201
    animal = response.json()
    animal_id = animal.get("idAnimal")

    # Traire la vache
    patch_url = f"{BASE_URL}/{animal_id}/traire"
    response = requests.patch(patch_url)
    print("Status code:", response.status_code)
    print("Response text:", response.text)
    assert response.status_code == 200

    data = response.json()
    assert data.get("litresReccoltes") == 10
    assert data.get("ecusGagnes") == 20

    # Nettoyer en supprimant la ferme creee (cascade sur animaux)
    delete_response = requests.delete(f"{URL_FERME}/{ferme_id}")
    assert delete_response.status_code == 204