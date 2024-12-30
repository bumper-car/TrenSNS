from kafka import KafkaProducer
import json
import requests
import os

# Kafka 설정
KAFKA_TOPIC = "youtube_comment"
KAFKA_SERVER = "localhost:9092"

# Kafka Producer 생성
producer = KafkaProducer(
    bootstrap_servers=KAFKA_SERVER,
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

API_KEY=os.getenv('API_KEY')

search_query = input("Enter your search query: ")
URL = f'https://www.googleapis.com/youtube/v3/search?part=snippet&q={search_query}&maxResults=3&type=video&key={API_KEY}'

# YouTube 검색 요청
response = requests.get(URL)

# 응답 상태 코드 확인
if response.status_code != 200:
    print(f"Error: Received status code {response.status_code}")
    print(response.text)
else:
    data = response.json()

    # 'items' 키가 있는지 확인
    if 'items' not in data:
        print("Error: 'items' key not found in response.")
        print(data)
    else:
        for item in data['items']:
            # 'videoId'가 없는 경우 무시
            if item['id']['kind'] != 'youtube#video':
                continue

            video_id = item['id']['videoId']

            # 댓글 데이터 가져오기
            try:
                comments_url = f'https://www.googleapis.com/youtube/v3/commentThreads?part=snippet&videoId={video_id}&maxResults=5&key={API_KEY}'
                comments_response = requests.get(comments_url)

                if comments_response.status_code == 200:
                    comments_data = comments_response.json()
                    for comment in comments_data.get('items', []):
                        snippet = comment['snippet']['topLevelComment']['snippet']
                        reply = snippet['textDisplay'].replace('\n', ' ')
                        like_count = int(snippet.get('likeCount', 0))
                        published_at = snippet['publishedAt']

                        # CommentModel 형식에 맞는 데이터 구성
                        comment_model = {
                            "id": comment['id'],  # 댓글 고유 ID
                            "video_id": video_id,
                            "reply": reply,
                            "like_count": like_count,
                            "published_at": published_at
                        }

                        # Kafka로 데이터 전송
                        try:
                            producer.send(KAFKA_TOPIC, comment_model)
                            print(f"Sent to Kafka: {comment_model}")
                        except Exception as e:
                            print(f"Error sending to Kafka: {e}")
                else:
                    print(f"Error: Failed to get comments for video ID {video_id}.")
            except Exception as e:
                print(f"Exception occurred while fetching comments for video ID {video_id}: {e}")

# Kafka Producer 종료
producer.flush()
producer.close()
