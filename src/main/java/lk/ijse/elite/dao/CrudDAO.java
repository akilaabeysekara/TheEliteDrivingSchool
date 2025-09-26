package lk.ijse.elite.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> extends SuperDAO {
    public boolean save(T dto) throws SQLException, ClassNotFoundException;

    public boolean update(T dto) throws SQLException, ClassNotFoundException;

    public String getNextId() throws SQLException, ClassNotFoundException;

    public String getLastId() throws SQLException, ClassNotFoundException;

    public Optional<T> findById(String selectedId) throws SQLException, ClassNotFoundException;

    public ArrayList<String> getAllIds() throws SQLException, ClassNotFoundException;

    public boolean delete(String ID) throws SQLException, ClassNotFoundException;

    public List<T> getAll() throws SQLException, ClassNotFoundException;
}