from rest_framework import serializers

class KinematicsSerializer(serializers.Serializer):
    u = serializers.FloatField(required=False, allow_null=True)
    v = serializers.FloatField(required=False, allow_null=True)
    a = serializers.FloatField(required=False, allow_null=True)
    t = serializers.FloatField(required=False, allow_null=True)
    s = serializers.FloatField(required=False, allow_null=True)

class DynamicsSerializer(serializers.Serializer):
    mass = serializers.FloatField(required=True)
    acceleration = serializers.FloatField(required=False, allow_null=True)
    force = serializers.FloatField(required=False, allow_null=True)

class EnergySerializer(serializers.Serializer):
    mass = serializers.FloatField(required=True)
    velocity = serializers.FloatField(required=True)
