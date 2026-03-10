import requests
import pytest
BASE_URL = "http://localhost:8080"

def test_acces_page_privee_bloque():
    # L'endpoint /user est censé renvoyer les infos du joueur connecté
    response = requests.get(f"{BASE_URL}/user")
    
    assert response.status_code == 401, f"Erreur de sécurité : L'accès devrait être bloqué (401), mais on a eu {response.status_code}"

def test_acces_page_publique_autorise():
    # L'index ou les assets doivent être publics
    response = requests.get(f"{BASE_URL}/index.html")

    assert response.status_code == 200, f"Le fichier public devrait être accessible, mais on a eu {response.status_code}"

def test_api_ouverte_pour_dev():
    response = requests.post(f"{BASE_URL}/api/fermes", json={"nom": "Ferme_Test_API"})
    
    assert response.status_code == 201, "L'API devrait être ouverte (201) pour le développement, mais elle est bloqué"
    
    ferme_id = response.json()["idFerme"]
    requests.delete(f"{BASE_URL}/api/fermes/{ferme_id}")