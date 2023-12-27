from pydantic import BaseModel

class UserBase(BaseModel):
    email: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int

    class Config:
        orm_mode = True

class PinEntryBase(BaseModel):
    text: str

class PinEntryCreate(PinEntryBase):
    pass

class PinEntry(PinEntryBase):
    entry_id: int
    user_id: int
    text: str

    class Config:
        orm_mode = True
