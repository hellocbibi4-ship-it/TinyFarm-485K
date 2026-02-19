import requests
import pytest

BASE_URL = "http://localhost:8080/api/"

def test_create_ferme():
    url = requests.get(BASE_URL + "/fermes")
    payload = {"nom": "Ferme Python"}
    response = requests.post(url, json=payload)
    
    assert response.status_code == 201
    data = response.json()
    assert data['soldeEcus'] == 1500
    assert data['score'] == 0
