import pytest
from sim._tsdz2 import ffi, lib as ebike # module generated from c-code
import numpy as np
from hypothesis import given, strategies as st

@given(
	end_value=st.integers(min_value=0, max_value=65535),
	init_value=st.integers(min_value=0, max_value=65535),
	alpha=st.integers(min_value=0, max_value=15))
def test_filter(init_value, end_value, alpha):
	filter_result = init_value

	# should converage at least twice as fast as the difference
	# additionally for small delta and large alpha it will converge linearly
	loops = round((abs(end_value - init_value) + alpha) / 2 + 0.5)
	for _ in range(loops):
		filter_result = ebike.filter(end_value, filter_result, alpha)

	assert filter_result == end_value, f'Expected filter_result {filter_result} == end_value {end_value} after {loops} loops'


@given(
	old_value=st.integers(min_value=0, max_value=65535),
	alpha=st.integers(min_value=0, max_value=15))
def test_filter_steady_state(old_value, alpha):
	new_value = old_value
	filter_result = ebike.filter(new_value, old_value, alpha)
	assert filter_result == old_value, f'Expected filter_result {filter_result} == new_value {old_value}'


@given(
	old_value=st.integers(min_value=0, max_value=65534),
	alpha=st.integers(min_value=0, max_value=15))
def test_filter_rising_by_one(old_value, alpha):
	new_value = old_value + 1
	filter_result = ebike.filter(new_value, old_value, alpha)
	assert filter_result == new_value, f'Expected filter_result {filter_result} == new_value {new_value}'

@given(
	old_value=st.integers(min_value=1, max_value=65535),
	alpha=st.integers(min_value=0, max_value=15))
def test_filter_falling_by_one(old_value, alpha):
	new_value = old_value - 1
	filter_result = ebike.filter(new_value, old_value, alpha)
	assert filter_result == new_value, f'Expected filter_result {filter_result} == new_value {new_value}'


# Run the tests
if __name__ == '__main__':
	pytest.main()