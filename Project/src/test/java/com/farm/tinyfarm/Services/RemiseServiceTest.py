
import requests
import pytest

# Compilation:
# pytest Project/src/test/java/com/farm/tinyfarm/Services/RemiseServiceTest.py -qsv

BASE_URL = "http://localhost:8080/api/remise"

#Fonction qui permet de créer une ferme et de la supprimer à la fin du test.
@pytest.fixture
def new_remise():
    # SETUP
    response = requests.post(BASE_URL, json={"nom": "Remise_Test"})
    remise = response.json()
    yield ferme  # Le test s'exécute ici
    # TEARDOWN
    requests.delete(f"{BASE_URL}/{ferme['idRemise']}")

def test_creer_ferme:
    