import requests
import pytest

BASE_URL = "http://localhost:8080/api/cooperative"

# Données de test
ID_ACHETEUR = 1
ID_ARTICLE = 1


def test_solde_insuffisant():
    payload= {
        "idAcheteur":1,
        "idArticle" : 1,
        "quantite":9999,
    }
    response = requests.post(f"{BASE_URL}/acheter", params=payload)
    assert response.status_code == 400

def test_statut_ouverture():
    """Vérifie si l'API répond correctement sur l'état d'ouverture"""
    response = requests.get(f"{BASE_URL}/statut")
    assert response.status_code == 200
    assert isinstance(response.json(), bool)


def test_achat_reussi_si_ouvert():
    """Teste l'achat seulement si la coopérative est ouverte"""
    if not requests.get(f"{BASE_URL}/statut").json():
        pytest.skip("Coopérative fermée : test d'achat ignoré.")

    payload = {"idAcheteur": ID_ACHETEUR, "idArticle": ID_ARTICLE, "quantite": 1}
    response = requests.post(f"{BASE_URL}/acheter", params=payload)
    
    assert response.status_code == 200
    assert "réussi" in response.text.lower()

def test_catalogue_acces_cohérent():
    """Vérifie que l'accès au catalogue correspond au statut d'ouverture"""
    is_open = requests.get(f"{BASE_URL}/statut").json()
    response = requests.get(f"{BASE_URL}/catalogue")

    if is_open:
        assert response.status_code == 200
        assert isinstance(response.json(), list)
    else:
        assert response.status_code == 403