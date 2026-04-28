# Module de tests Pytest pour le service animal de TinyFarm.

import time
import uuid

import requests

# Lancement:
# pytest project/tests/python/services/test_animal_service.py -qsv

BASE_URL = "http://localhost:8080/api"
URL_FERME = f"{BASE_URL}/fermes"


def _create_ferme(prefix="FermeAnimalCtrl"):
    short_prefix = prefix[:9]
    payload = {"nom": f"{short_prefix}_{uuid.uuid4().hex[:6]}"}
    response = requests.post(URL_FERME, json=payload, timeout=10)
    assert response.status_code == 201, response.text
    return response.json()


def _delete_ferme(ferme_id):
    requests.delete(f"{URL_FERME}/{ferme_id}", timeout=10)


def _acheter_animal(ferme_id, nom, type_animal, **extra):
    payload = {"nom": nom, "typeAnimal": type_animal, **extra}
    return requests.post(f"{URL_FERME}/{ferme_id}/animaux", json=payload, timeout=10)


def test_get_animaux_retourne_404_si_ferme_inexistante():
    response = requests.get(f"{BASE_URL}/fermes/999999/animaux", timeout=10)
    assert response.status_code == 404


def test_get_animaux_retourne_liste_pour_une_ferme_existante():
    ferme = _create_ferme("FermeListe")
    try:
        response = requests.get(f"{BASE_URL}/fermes/{ferme['idFerme']}/animaux", timeout=10)
        assert response.status_code == 200, response.text
        assert isinstance(response.json(), list)
    finally:
        _delete_ferme(ferme["idFerme"])


def test_acheter_animal_refuse_type_invalide():
    ferme = _create_ferme("FermeTypeInvalide")
    try:
        response = requests.post(
            f"{URL_FERME}/{ferme['idFerme']}/animaux",
            json={"nom": "Mystere"},
            timeout=10,
        )
        assert response.status_code == 400
        assert "invalide" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_acheter_animal_cree_un_animal():
    ferme = _create_ferme("FermeAchat")
    try:
        response = _acheter_animal(ferme["idFerme"], "Bessie", "VACHE", stockLaitPis=0)
        assert response.status_code == 201, response.text
        body = response.json()
        assert body.get("idAnimal") is not None
        assert body.get("typeAnimal") == "VACHE"
    finally:
        _delete_ferme(ferme["idFerme"])


def test_nourrir_refuse_action_pour_lapin():
    ferme = _create_ferme("FermeNourrir")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "Lapinou", "LAPIN")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/nourrir", timeout=10)
        assert response.status_code == 400
        assert "action non disponible" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_abreuver_refuse_action_pour_lapin():
    ferme = _create_ferme("FermeAbreuver")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "Pompom", "LAPIN")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/abreuver", timeout=10)
        assert response.status_code == 400
        assert "action non disponible" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_soigner_retourne_404_pour_animal_inexistant():
    response = requests.patch(f"{BASE_URL}/animaux/999999/soigner", timeout=10)
    assert response.status_code == 404


def test_soigner_refuse_action_pour_lapin():
    ferme = _create_ferme("FermeSoigner")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "LapiSick", "LAPIN")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/soigner", timeout=10)
        assert response.status_code == 400
        assert "action non disponible" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_nettoyer_refuse_pour_poule():
    ferme = _create_ferme("FermeNettoyer")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "Cocotte", "POULE")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/nettoyer", timeout=10)
        assert response.status_code == 400
        assert "vaches" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_produire_lait_refuse_pour_poule():
    ferme = _create_ferme("FermeProdLait")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "Pondeuse", "POULE")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/produire-lait", timeout=10)
        assert response.status_code == 400
        assert "vaches" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])


def test_traire_vache_retourne_lait_et_ecus():
    ferme = _create_ferme(f"FermeTraite{int(time.time()) % 1000000}")
    try:
        create_resp = _acheter_animal(
            ferme["idFerme"],
            nom="VacheTest01",
            type_animal="VACHE",
            stockLaitPis=10,
        )
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/traire", timeout=10)
        assert response.status_code == 200, response.text

        data = response.json()
        assert data.get("litresReccoltes") == 10
        assert data.get("ecusGagnes") == 20
    finally:
        _delete_ferme(ferme["idFerme"])


def test_traire_refuse_pour_poule():
    ferme = _create_ferme("FermeTrairePoule")
    try:
        create_resp = _acheter_animal(ferme["idFerme"], "PouleTraite", "POULE")
        assert create_resp.status_code == 201, create_resp.text
        animal_id = create_resp.json()["idAnimal"]

        response = requests.patch(f"{BASE_URL}/animaux/{animal_id}/traire", timeout=10)
        assert response.status_code == 400
        assert "vaches" in response.text.lower()
    finally:
        _delete_ferme(ferme["idFerme"])