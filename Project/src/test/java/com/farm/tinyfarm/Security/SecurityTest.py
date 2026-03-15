import requests
import pytest
BASE_URL = "http://localhost:8080"

def test_acces_page_privee_bloque():
    response = requests.get(f"{BASE_URL}/api/me")
    assert response.status_code == 401

def test_acces_page_publique_autorise():
    # L'index ou les assets doivent être publics
    response = requests.get(f"{BASE_URL}/index.html")

    assert response.status_code == 200, f"Le fichier public devrait être accessible, mais on a eu {response.status_code}"


#def test_api_ouverte_pour_dev():
#    response = requests.post(f"{BASE_URL}/api/fermes", json={"nom": "Ferme_Test_API"})
    
#    assert response.status_code == 201, "L'API devrait être ouverte (201) pour le développement, mais elle est bloqué"
    
#    ferme_id = response.json()["idFerme"]
#    requests.delete(f"{BASE_URL}/api/fermes/{ferme_id}")


def test_assets_publics():
    # CSS, JS, images doivent être accessibles sans connexion
    for url in ["/css/style.css", "/js/script.js", "/assets/"]:
        response = requests.get(f"{BASE_URL}{url}")
        assert response.status_code == 200, f"{url} devrait être public"

def test_login_github_redirige():
    # Le endpoint OAuth2 doit rediriger vers GitHub
    response = requests.get(f"{BASE_URL}/oauth2/authorization/github", allow_redirects=False)
    assert response.status_code == 302, "Doit rediriger vers GitHub"
    assert "github.com" in response.headers.get("Location", ""), "La redirection doit pointer vers GitHub"

# il faut ajouter auth/test-token pour que ça fonctionne
#def test_api_me_avec_token(github_pat):
#    # Avec un vrai token, /api/me doit renvoyer 200
#    headers = {"Authorization": f"Bearer {github_pat}"}
#    response = requests.get(f"{BASE_URL}/api/me", headers=headers)
#    assert response.status_code == 200
#    assert "login" in response.json(), "La réponse doit contenir le login GitHub"