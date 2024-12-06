from pytrends.request import TrendReq
import pandas as pd
import json
from datetime import datetime
import time  # 요청 간 지연을 위해 추가

# PyTrends 설정
pytrends = TrendReq(hl='ko', tz=540, retries=3, backoff_factor=0.1)

# 사용자로부터 검색어 입력 받기
search_query = input("Enter your search query: ")

# 검색어 및 매개변수 설정
kw_list = [search_query]
timeframe = 'today 1-m'
geo = 'KR'

# 데이터 수집 함수 정의 (429 에러 방지)
def fetch_trends_data(pytrends, kw_list, timeframe, geo):
    while True:
        try:
            # 페이로드 빌드
            pytrends.build_payload(kw_list, cat=0, timeframe=timeframe, geo=geo)
            # 데이터 가져오기
            interest_over_time_df = pytrends.interest_over_time()
            interest_by_region_df = pytrends.interest_by_region(resolution='COUNTRY', inc_low_vol=True, inc_geo_code=False)
            return interest_over_time_df, interest_by_region_df
        except Exception as e:
            if '429' in str(e):
                print("429 Error: Too many requests. Retrying after 60 seconds...")
                time.sleep(60)  # 1분 대기
            else:
                print(f"An error occurred: {e}")
                raise

# 데이터 수집
interest_over_time_df, interest_by_region_df = fetch_trends_data(pytrends, kw_list, timeframe, geo)

# Elasticsearch bulk 형식으로 데이터 변환
bulk_data = []

# 시간에 따른 관심도 데이터 변환
for index, row in interest_over_time_df.iterrows():
    action = {
        "index": {
            "_index": "google_trend",
            "_id": f"time{index.strftime('%Y%m%d%H%M%S')}"
        }
    }
    source = {
        "timestamp": index.strftime('%Y-%m-%dT%H:%M:%S'),
        "keyword": kw_list[0],
        "interest": int(row[kw_list[0]]),
        "type": "time_series"
    }
    bulk_data.append(json.dumps(action))
    bulk_data.append(json.dumps(source))

# 지역별 관심도 데이터 변환
for index, row in interest_by_region_df.iterrows():
    action = {
        "index": {
            "_index": "google_trend",
            "_id": f"region{index}"
        }
    }
    source = {
        "timestamp": datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
        "keyword": kw_list[0],
        "region": index,
        "interest": int(row[kw_list[0]]),
        "type": "region"
    }
    bulk_data.append(json.dumps(action))
    bulk_data.append(json.dumps(source))

# Elasticsearch bulk 형식으로 출력
print("\n".join(bulk_data))
