import requests
import pytest

# URL de ton serveur local
BASE_URL = "http://localhost:8080"

def test_acces_page_privee_bloque():
    """
    Test pertinent n°1 : Vérifier qu'un robot non connecté 
    se fait bloquer s'il tente d'accéder aux infos de session.
    """
    # L'endpoint /user est censé renvoyer les infos du joueur connecté
    response = requests.get(f"{BASE_URL}/user")
    
    # On s'attend à une erreur 401 (Unauthorized)
    assert response.status_code == 401, f"Erreur de sécurité : L'accès devrait être bloqué (401), mais on a eu {response.status_code}"

def test_acces_page_publique_autorise():
    """
    Test pertinent n°2 : Vérifier que les pages publiques (comme l'accueil ou le CSS)
    restent accessibles à tous.
    """
    # L'index ou les assets doivent être publics
    response = requests.get(f"{BASE_URL}/index.html")
    
    # On s'attend à un succès 200 (OK)
    assert response.status_code == 200, f"Le fichier public devrait être accessible, mais on a eu {response.status_code}"

def test_api_ouverte_pour_dev():
    """
    Test pertinent n°3 : Vérifier que l'API reste ouverte pour que
    les tests des autres membres du groupe continuent de fonctionner.
    """
    response = requests.post(f"{BASE_URL}/api/fermes", json={"nom": "Ferme_Test_API"})
    
    # Si on a 401 ou 403, c'est que tu as bloqué tes camarades !
    assert response.status_code == 201, "L'API devrait être ouverte (201) pour le développement, mais elle semble bloquée."
    
    # Nettoyage
    ferme_id = response.json()["idFerme"]
    requests.delete(f"{BASE_URL}/api/fermes/{ferme_id}")