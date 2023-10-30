from typing import Union

from pydantic import BaseModel


class PinEntryBase(BaseModel):
    image: str
    text: Union[str, None] = None


class PinEntryCreate(PinEntryBase):
    pass


class PinEntry(PinEntryBase):
    entry_id: int
    user_id: int

    class Config:
        orm_mode = True


class UserBase(BaseModel):
    email: str


class UserCreate(UserBase):
    password: str


class User(UserBase):
    id: int

    class Config:
        orm_mode = True
