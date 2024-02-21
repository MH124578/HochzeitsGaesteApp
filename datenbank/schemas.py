from datetime import time, date
from typing import Union
import re


from pydantic import BaseModel, validator


class UserBase(BaseModel):
    email: str


class UserCreate(UserBase):
    name: str
    password: str
    birthdate: str
    profile_picture: bytes


class User(UserBase):
    id: int

    class Config:
        orm_mode = True


class HomeInformationEntryBase(BaseModel):
    title: str


class HomeInformationEntryCreate(HomeInformationEntryBase):
    description: str
    information_time: str
    event_today: bool

    @validator("information_time")
    def validate_information_time_format(cls, value):
        match = re.match(r'^([01]\d|2[0-3]):([0-5]\d)$', value)
        if not match:
            raise ValueError("Invalid time format. Use HH:MM.")
        return value


class HomeInformationEntry(HomeInformationEntryCreate):
    entry_id: int
    user_id: int


class PinEntryBase(BaseModel):
    image: str
    text: Union[str, None] = None


class PinEntryCreate(PinEntryBase):
    image: bytes 


class PinEntry(PinEntryBase):
    entry_id: int
    user_id: int

    class Config:
        orm_mode = True


class RelationshipBase(BaseModel):
    relationship_type: str

class RelationshipCreate(RelationshipBase):
    guest_id_1: int
    guest_id_2: int

class Relationship(RelationshipBase):
    id: int
    guest_id_1: int
    guest_id_2: int

class RoleBase(BaseModel):
    role_name: str

class RoleCreate(RoleBase):
    pass

class Role(RoleBase):
    id: int

class GuestRoleBase(BaseModel):
    user_id: int
    role_id: int

class GuestRoleCreate(GuestRoleBase):
    pass

class GuestRole(GuestRoleBase):
    id: int

class FamilyBase(BaseModel):
    family_name: str

class FamilyCreate(FamilyBase):
    pass

class Family(FamilyBase):
    id: int

class FamilyMemberBase(BaseModel):
    user_id: int
    family_id: int

class FamilyMember(FamilyMemberBase):
    id: int

class FamilyMemberCreate(FamilyMemberBase):
    pass
