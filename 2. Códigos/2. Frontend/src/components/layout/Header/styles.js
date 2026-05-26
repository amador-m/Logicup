import styled from "styled-components";

export const Container = styled.header`
  width: 100%;
  height: 82px;
  padding: 0 120px;
  background-color: var(--brown-dark);
  color: var(--cream);
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const Img = styled.div`
    img{
        width: 170px;
    }
`
export const Logo = styled.h1`
  font-size: 30px;
  font-weight: 800;

  span {
    color: var(--yellow);
  }
`;

export const Menu = styled.ul`
  display: flex;
  gap: 30px;
`;

export const MenuItem = styled.li`
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.3s;

  &:hover {
    color: var(--yellow);
  }
`;

export const Actions = styled.div`
  display: flex;
  gap: 12px;
`;

export const LoginButton = styled.button`
  background-color: transparent;
  color: var(--cream);
  border: 2px solid var(--cream);
  padding: 10px 22px;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;

    &:hover {
        background-color: var(--green);
        color: var(--white);
    }
`;

export const RegisterButton = styled.button`
  background-color: var(--yellow);
  color: var(--brown-dark);
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background-color: var(--orange);
    color: var(--white);
  }
`;