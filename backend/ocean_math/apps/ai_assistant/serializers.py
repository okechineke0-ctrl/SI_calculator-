from rest_framework import serializers

class AiSolveSerializer(serializers.Serializer):
    prompt = serializers.CharField(max_length=5000, required=True)
    context = serializers.CharField(max_length=1000, required=False, allow_blank=True)
