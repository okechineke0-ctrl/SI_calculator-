from rest_framework import serializers

class BaseConvertSerializer(serializers.Serializer):
    value = serializers.CharField(max_length=100, required=True)
    from_base = serializers.IntegerField(default=10, min_value=2, max_value=36)
    to_base = serializers.IntegerField(default=2, min_value=2, max_value=36)

class BitwiseSerializer(serializers.Serializer):
    a = serializers.IntegerField(required=True)
    b = serializers.IntegerField(default=0)
    operation = serializers.ChoiceField(choices=['and', 'or', 'xor', 'not', 'nand', 'nor', 'shl', 'shr'], default='and')
    bit_width = serializers.ChoiceField(choices=[8, 16, 32, 64], default=32)

class AsciiSerializer(serializers.Serializer):
    text = serializers.CharField(max_length=500, required=True)

class TruthTableSerializer(serializers.Serializer):
    variables = serializers.ListField(child=serializers.CharField(max_length=10))
    expression = serializers.CharField(max_length=200, required=True)
