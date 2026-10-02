from rest_framework import serializers

class UnitConvertSerializer(serializers.Serializer):
    value = serializers.FloatField(required=True)
    from_unit = serializers.CharField(max_length=50, required=True)
    to_unit = serializers.CharField(max_length=50, required=True)
