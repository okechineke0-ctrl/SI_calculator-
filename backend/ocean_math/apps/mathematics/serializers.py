from rest_framework import serializers

class EvaluateSerializer(serializers.Serializer):
    expression = serializers.CharField(max_length=1000, required=True)
    precision = serializers.IntegerField(default=15, min_value=1, max_value=100)
    angle_mode = serializers.ChoiceField(choices=['DEG', 'RAD'], default='DEG')

class DifferentiateSerializer(serializers.Serializer):
    expression = serializers.CharField(max_length=1000, required=True)
    variable = serializers.CharField(max_length=10, default='x')
    order = serializers.IntegerField(default=1, min_value=1, max_value=10)

class IntegrateSerializer(serializers.Serializer):
    expression = serializers.CharField(max_length=1000, required=True)
    variable = serializers.CharField(max_length=10, default='x')
    lower_limit = serializers.FloatField(required=False, allow_null=True)
    upper_limit = serializers.FloatField(required=False, allow_null=True)

class FactorExpandSerializer(serializers.Serializer):
    expression = serializers.CharField(max_length=1000, required=True)
    operation = serializers.ChoiceField(choices=['factor', 'expand', 'simplify'], default='factor')

class MatrixSerializer(serializers.Serializer):
    matrix = serializers.ListField(
        child=serializers.ListField(child=serializers.FloatField())
    )
    operation = serializers.ChoiceField(choices=['det', 'inv', 'rank'], default='det')
