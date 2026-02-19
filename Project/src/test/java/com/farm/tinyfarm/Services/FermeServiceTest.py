import requests
import pytest

# Compilation:
# pytest Project/src/test/java/com/farm/tinyfarm/Services/FermeServiceTest.py -q

BASE_URL = "http://localhost:8080/api"

def test_create_ferme():
    url = BASE_URL + "/fermes"
    payload = {"nom": "FermePython"}
    response = requests.post(url, json=payload)
    
    assert response.status_code == 201
    data = response.json()
    assert data['soldeEcus'] == 1500
    assert data['score'] == 0
