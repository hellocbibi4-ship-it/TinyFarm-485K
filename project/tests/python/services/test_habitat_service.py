# Module de tests Pytest pour le service habitat de TinyFarm.

import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_habitat_service.py -qsv

BASE_URL = "http://localhost:8080/api/fermes"


@pytest.fixture
def new_ferme():
    # SETUP
    response = requests.post(BASE_URL, json={"nom": "Ferme_Test"})
    assert response.status_code == 201
    ferme = response.json()
    yield ferme
    # TEARDOWN
    requests.delete(f"{BASE_URL}/{ferme['idFerme']}")


def test_clapier_status_par_defaut(new_ferme):
    # Une ferme neuve demarre avec 8 lapins, aucun malade.
    ferme_id = new_ferme["idFerme"]
    response = requests.get(f"{BASE_URL}/{ferme_id}/animaux/clapier")

    assert response.status_code == 200
    data = response.json()
    assert data["totalLapins"] == 8
    assert data["sickLapins"] == 0
    assert data["hungryLapins"] == 0
    assert data["thirstyLapins"] == 0


def test_lapins_status_par_defaut(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.get(f"{BASE_URL}/{ferme_id}/animaux/lapins/status")

    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 8


def test_poules_status_par_defaut(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.get(f"{BASE_URL}/{ferme_id}/animaux/poules/status")

    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 4


def test_vaches_status_par_defaut(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.get(f"{BASE_URL}/{ferme_id}/animaux/vaches/status")

    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 1


def test_acheter_poule(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.post(
        f"{BASE_URL}/{ferme_id}/acheter-animal", params={"type": "poule"}
    )
    assert response.status_code == 200

    status = requests.get(f"{BASE_URL}/{ferme_id}/animaux/poules/status").json()
    assert status["total"] == 5


def test_acheter_lapin(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.post(
        f"{BASE_URL}/{ferme_id}/acheter-animal", params={"type": "lapin"}
    )
    assert response.status_code == 200

    status = requests.get(f"{BASE_URL}/{ferme_id}/animaux/lapins/status").json()
    assert status["total"] == 9


def test_acheter_deuxieme_vache_refuse(new_ferme):
    # La ferme commence deja avec 1 vache : en acheter une autre doit echouer.
    ferme_id = new_ferme["idFerme"]
    response = requests.post(
        f"{BASE_URL}/{ferme_id}/acheter-animal", params={"type": "vache"}
    )
    assert response.status_code == 400


def test_acheter_type_inconnu_refuse(new_ferme):
    ferme_id = new_ferme["idFerme"]
    response = requests.post(
        f"{BASE_URL}/{ferme_id}/acheter-animal", params={"type": "dragon"}
    )
    assert response.status_code == 400