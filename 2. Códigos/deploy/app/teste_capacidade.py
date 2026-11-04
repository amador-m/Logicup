#!/usr/bin/env python3
# =====================================================================
# LogiCup — teste de CAPACIDADE (Sprint 10). Rode na VM logicup-app, na pasta deploy/app:
#   python3 teste_capacidade.py
#
# Simula uma turma usando o site ao mesmo tempo e mede quanto tempo cada pedido leva:
#   1) 40 pessoas abrindo o site juntas (GET /api/auth/csrf, passa por Caddy + Spring);
#   2) 20 logins errados juntos (mede o BCrypt, a parte mais "pesada" do login);
#   3) 30 programas Java executados juntos DIRETO no Piston (o gargalo esperado).
#
# Não cria contas, não grava nada no banco e não usa senhas reais. Os 20 logins errados usam um
# e-mail que não existe e contam no limite de erros por IP desta VM (esquecido em 15 minutos).
# Só usa a biblioteca padrão do Python (nada para instalar).
# =====================================================================
import concurrent.futures as cf
import http.cookiejar
import json
import statistics
import time
import urllib.error
import urllib.request

SITE = "https://logicup.dev.br"
PISTON = "http://10.10.0.3:2000/api/v2/execute"
PROGRAMA = 'public class Main { public static void main(String[] a) { int s = 0; for (int i = 1; i <= 7; i++) s += i; System.out.println(s); } }'


def medir(nome, funcao, quantidade):
    """Dispara `quantidade` chamadas de `funcao` AO MESMO TEMPO e resume os tempos."""
    inicio = time.perf_counter()
    with cf.ThreadPoolExecutor(max_workers=quantidade) as pool:
        resultados = list(pool.map(lambda _: funcao(), range(quantidade)))
    total = time.perf_counter() - inicio
    tempos = sorted(t for ok, t, _ in resultados if ok)
    falhas = [det for ok, _, det in resultados if not ok]
    print(f"\n== {nome}: {quantidade} ao mesmo tempo, {total:.1f} s no total")
    if tempos:
        print(f"   ok: {len(tempos)} | mais rápido {tempos[0]:.2f} s | mediana {statistics.median(tempos):.2f} s"
              f" | mais lento {tempos[-1]:.2f} s")
    if falhas:
        print(f"   FALHAS: {len(falhas)} -> {sorted(set(falhas))[:5]}")


def abrir_site():
    t = time.perf_counter()
    try:
        with urllib.request.urlopen(SITE + "/api/auth/csrf", timeout=30) as r:
            return r.status == 204, time.perf_counter() - t, r.status
    except Exception as e:  # noqa: BLE001
        return False, time.perf_counter() - t, str(e)[:60]


def login_errado():
    jar = http.cookiejar.CookieJar()
    abridor = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
    abridor.open(SITE + "/api/auth/csrf", timeout=30)
    csrf = next(c.value for c in jar if c.name == "__Host-XSRF-TOKEN")
    corpo = json.dumps({"login": "teste.capacidade@nao-existe.invalid", "senha": "senhaErrada123"}).encode()
    pedido = urllib.request.Request(SITE + "/api/auth/login", data=corpo, method="POST", headers={
        "Content-Type": "application/json", "X-XSRF-TOKEN": csrf, "Origin": SITE})
    t = time.perf_counter()
    try:
        abridor.open(pedido, timeout=30)
        return False, time.perf_counter() - t, "login aceito?!"
    except urllib.error.HTTPError as e:
        return e.code == 401, time.perf_counter() - t, e.code   # 401 = senha errada: o esperado


def executar_java():
    corpo = json.dumps({"language": "java", "version": "15.0.2",
                        "files": [{"name": "Main.java", "content": PROGRAMA}],
                        "run_timeout": 6000, "compile_timeout": 10000}).encode()
    pedido = urllib.request.Request(PISTON, data=corpo, method="POST", headers={"Content-Type": "application/json"})
    t = time.perf_counter()
    try:
        with urllib.request.urlopen(pedido, timeout=120) as r:
            saida = json.load(r).get("run", {}).get("stdout", "").strip()
            return saida == "28", time.perf_counter() - t, saida[:30]
    except Exception as e:  # noqa: BLE001
        return False, time.perf_counter() - t, str(e)[:60]


if __name__ == "__main__":
    medir("Abrir o site", abrir_site, 40)
    medir("Login (senha errada)", login_errado, 20)
    medir("Executar Java no Piston", executar_java, 30)
    print("\nCopie TUDO acima e mande no chat.")
