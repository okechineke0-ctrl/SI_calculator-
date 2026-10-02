from rest_framework import serializers

class MolarMassSerializer(serializers.Serializer):
    formula = serializers.CharField(max_length=100, required=True)

class BalanceSerializer(serializers.Serializer):
    equation = serializers.CharField(max_length=500, required=True)
