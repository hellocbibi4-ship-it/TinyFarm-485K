import requests
import pytest

BASE_URL = "http://localhost:8080"


def test_acces_page_privee_bloque():
    response = requests.get(f"{BASE_URL}/api/me")
    assert response.status_code == 401


def test_acces_page_publique_autorise():
    # L'index ou les assets doivent etre publics
    response = requests.get(f"{BASE_URL}/index.html")

    assert response.status_code == 200, (
        f"Le fichier public devrait etre accessible, mais on a eu {response.status_code}"
    )


# def test_api_ouverte_pour_dev():
#     response = requests.post(f"{BASE_URL}/api/fermes", json={"nom": "Ferme_Test_API"})
#
#     assert response.status_code == 201, (
#         "L'API devrait etre ouverte (201) pour le developpement, mais elle est bloque"
#     )
#
#     ferme_id = response.json()["idFerme"]
#     requests.delete(f"{BASE_URL}/api/fermes/{ferme_id}")


def test_assets_publics():
    # CSS, JS, images doivent etre accessibles sans connexion
    for url in ["/css/style.css", "/js/script.js", "/assets/"]:
        response = requests.get(f"{BASE_URL}{url}")
        assert response.status_code == 200, f"{url} devrait etre public"


def test_login_github_redirige():
    # Le endpoint OAuth2 doit rediriger vers GitHub
    response = requests.get(f"{BASE_URL}/oauth2/authorization/github", allow_redirects=False)
    assert response.status_code == 302, "Doit rediriger vers GitHub"
    assert "github.com" in response.headers.get("Location", ""), (
        "La redirection doit pointer vers GitHub"
    )


def test_api_logout_url_public():
    # Endpoint utilitaire accessible sans auth.
    response = requests.get(f"{BASE_URL}/api/logout-url")
    assert response.status_code == 200
    assert response.json()["url"] == "/logout"


def test_api_oauth_status_public():
    response = requests.get(f"{BASE_URL}/api/auth/oauth-status")
    assert response.status_code == 200
    data = response.json()
    assert "githubConfigured" in data
    assert "authorizationUrl" in data


def test_api_github_farm_use_sans_auth():
    # Acces refuse si pas connecte a GitHub.
    response = requests.post(f"{BASE_URL}/api/auth/github/farm/use")
    assert response.status_code == 401


def test_api_github_farm_new_sans_auth():
    response = requests.post(f"{BASE_URL}/api/auth/github/farm/new")
    assert response.status_code == 401


def test_api_github_farm_reset_sans_auth():
    response = requests.post(f"{BASE_URL}/api/auth/github/farm/reset")
    assert response.status_code == 401


def test_me_retourne_erreur_json():
    response = requests.get(f"{BASE_URL}/api/me")
    assert response.status_code == 401
    assert response.json()["error"] == "Not authenticated"


# il faut ajouter auth/test-token pour que ca fonctionne
# def test_api_me_avec_token(github_pat):
#     # Avec un vrai token, /api/me doit renvoyer 200
#     headers = {"Authorization": f"Bearer {github_pat}"}
#     response = requests.get(f"{BASE_URL}/api/me", headers=headers)
#     assert response.status_code == 200
#     assert "login" in response.json(), "La reponse doit contenir le login GitHub"