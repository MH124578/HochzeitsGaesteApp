from pydantic import BaseModel
from typing import Optional

class CategoryBase(BaseModel):
    name: str

class CategoryCreate(CategoryBase):
    pass

class Category(CategoryBase):
    id: int
    
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

class PinEntryBase(BaseModel):
    text: str
    category_id: int

class PinEntryCreate(PinEntryBase):
    pass

class PinEntry(PinEntryBase):
    entry_id: int
    user_id: int
    text: str
    category: 'Category'

    class Config:
        orm_mode = True
